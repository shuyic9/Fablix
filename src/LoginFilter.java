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
        // Using HashSet for O(1) lookup
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("login.css");
        allowedURIs.add("api/login");
        allowedURIs.add("logo.png");
        allowedURIs.add("_dashboard.html");
        allowedURIs.add("_dashboard.js");
        allowedURIs.add("_dashboard.css");
        allowedURIs.add("api/_dashboard");
        allowedURIs.add("fablix/");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI().toLowerCase();
        System.out.println("LoginFilter: " + requestURI);

        if (isUrlAllowedWithoutLogin(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        if (httpRequest.getSession().getAttribute("user") == null) {
            // Redirects to login page if no user is found in the session
            httpResponse.sendRedirect("login.html");
        } else {
            // User is authenticated, proceed
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        // Check if request URI ends with any allowed URI
        return allowedURIs.stream().anyMatch(requestURI::endsWith);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}