import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation that handles access control based on user authentication
 * and manages redirects for both authenticated and unauthenticated access to various resources.
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private final ArrayList<String> dashboardURIs = new ArrayList<>();
    private final ArrayList<String> notAllowedDashboardURIs = new ArrayList<>();

    /**
     * Initialize the filter with specific URLs that can be accessed without authentication
     * and with special handling based on user roles and access rights.
     */
    public void init(FilterConfig fConfig) {
        // URIs that can be accessed without needing to be logged in
        allowedURIs.add("/login.html");
        allowedURIs.add("/login.js");
        allowedURIs.add("/api/login");
        allowedURIs.add("/login.css");
        allowedURIs.add("logo.png");
        allowedURIs.add("/api/_dashboard");
        allowedURIs.add("/_dashboard.html");
        allowedURIs.add("/_dashboard.js");
        allowedURIs.add("/_dashboard");

        // URIs specifically used for dashboard access
        dashboardURIs.add("/_dashboard");

        // URIs that are not allowed for non-admin users
        notAllowedDashboardURIs.add("/dashboard.html");
        notAllowedDashboardURIs.add("/dashboard");
    }

    /**
     * The main filter action that checks each HTTP request against allowed URIs and user authentication.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI().toLowerCase();

        // Allow requests to proceed if the URI is in the list of allowed URIs
        if (this.isUrlAllowedWithoutLogin(requestURI)) {
            chain.doFilter(request, response);
        } else {
            // Check user authentication status
            if (httpRequest.getSession().getAttribute("user") == null) {
                // Redirect to login page if not logged in
                if (dashboardURIs.stream().anyMatch(uri -> requestURI.endsWith(uri))) {
                    httpResponse.sendRedirect("/_dashboard.html");
                } else {
                    httpResponse.sendRedirect("/login.html");
                }
            } else {
                // For logged-in users, check if they are trying to access restricted areas
                if (!Boolean.TRUE.equals(httpRequest.getSession().getAttribute("admin")) && notAllowedDashboardURIs.stream().anyMatch(uri -> requestURI.endsWith(uri))) {
                    httpResponse.sendRedirect("/main.html");
                } else {
                    // Allow access if none of the special conditions are met
                    chain.doFilter(request, response);
                }
            }
        }
    }

    /**
     * Helper method to check if a given URI is in the list of allowed URIs.
     */
    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI::endsWith);
    }

    /**
     * Optional cleanup logic when the filter is being taken out of service.
     */
    public void destroy() {
        // Cleanup resources if necessary
    }
}
