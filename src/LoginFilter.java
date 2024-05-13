import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    @Override
    public void init(FilterConfig fConfig) {
        // Existing allowed URIs
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("login.css");
        allowedURIs.add("api/login");
        allowedURIs.add("logo.png");
        allowedURIs.add("api/_dashboard");
        allowedURIs.add("_dashboard.js");
        allowedURIs.add("_dashboard.html");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        if (httpRequest.getSession().getAttribute("user") == null) {
            if (httpRequest.getRequestURI().contains("_dashboard")) {
                httpResponse.sendRedirect("_dashboard.html");
            } else {
                httpResponse.sendRedirect("login.html");}
        } else {
            if (this.isRestrictedPage(httpRequest.getRequestURI())) {
                Boolean isAdmin = (Boolean) httpRequest.getSession().getAttribute("admin");
                if (Boolean.TRUE.equals(isAdmin)) {
                    chain.doFilter(request, response);
                } else {
                    httpResponse.sendRedirect("_dashboard.html");
                }
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(uri -> requestURI.toLowerCase().endsWith(uri));
    }

    private boolean isRestrictedPage(String requestURI) {
        return requestURI.contains("dashboard.html") || requestURI.contains("addStar.html") || requestURI.contains("addMovie.html");
    }

    @Override
    public void destroy() {
        // ignored.
    }
}
