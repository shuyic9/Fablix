import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final Set<String> allowedURIs = new HashSet<>();

    @Override
    public void init(FilterConfig fConfig) {
        String contextPath = fConfig.getServletContext().getContextPath();

        // Paths that are accessible without authentication
        allowedURIs.add(contextPath + "/login.html");
        allowedURIs.add(contextPath + "/login.js");
        allowedURIs.add(contextPath + "/login.css");
        allowedURIs.add(contextPath + "/api/login");
        allowedURIs.add("logo.png");
        allowedURIs.add(contextPath + "/api/_dashboard");
        allowedURIs.add(contextPath + "/dashboard");
        allowedURIs.add(contextPath + "/_dashboard.html");
        allowedURIs.add(contextPath + "/api/_dashboard");

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI().toLowerCase();

        // Check for direct access to the logical URI
        if (requestURI.endsWith("/fablix/dashboard")) {
            // Redirect directly to the _dashboard.html page
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard.html");
            return;
        }

        // If the request is allowed without logging in, continue processing
        if (isUrlAllowedWithoutLogin(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Default action for non-authenticated users trying to access other resources
        if (httpRequest.getSession().getAttribute("user") == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
        } else {
            // If user is logged in, continue processing
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI::endsWith);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}
