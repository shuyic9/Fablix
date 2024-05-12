import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    // Store allowed URIs in a HashSet for efficient lookup
    private final Set<String> allowedURIs = new HashSet<>();

    @Override
    public void init(FilterConfig fConfig) {
        // Retrieve the context path to handle application URL prefixes correctly
        String contextPath = fConfig.getServletContext().getContextPath();

        // Add all allowed URIs to the set, including the context path
        allowedURIs.add(contextPath + "/login.html");
        allowedURIs.add(contextPath + "/login.js");
        allowedURIs.add(contextPath + "/login.css");
        allowedURIs.add(contextPath + "/api/login");
        allowedURIs.add("logo.png");
        allowedURIs.add(contextPath + "/_dashboard.html");
        allowedURIs.add(contextPath + "/_dashboard.js");
        allowedURIs.add(contextPath + "/_dashboard.css");
        allowedURIs.add(contextPath + "/api/_dashboard");
        allowedURIs.add(contextPath + "/fablix/");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get the request URI and convert to lowercase
        String requestURI = httpRequest.getRequestURI().toLowerCase();

        // Log for debugging purposes
        System.out.println("LoginFilter: Request URI: " + requestURI);
        System.out.println("LoginFilter: Full URL: " + httpRequest.getRequestURL());

        // If the request is allowed without logging in, continue processing
        if (isUrlAllowedWithoutLogin(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Check if user is logged in by verifying session attribute
        if (httpRequest.getSession().getAttribute("user") == null) {
            // Redirect to login page if user is not logged in
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
        } else {
            // If user is logged in, continue processing
            chain.doFilter(request, response);
        }
    }

    /**
     * Check if the given request URI is allowed without logging in.
     *
     * @param requestURI The request URI to check
     * @return True if the URI is allowed, false otherwise
     */
    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI::endsWith);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed (currently none)
    }
}
