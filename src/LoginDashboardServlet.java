import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginDashboardServlet", urlPatterns = "/api/_dashboard")
public class LoginDashboardServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init() {
        try {
            // Adjust the database lookup string to point to your employee database
            InitialContext initialContext = new InitialContext();
            dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("username"); // Retrieve email (username) from the request
        String password = request.getParameter("password");
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        JsonObject responseJsonObject = new JsonObject();

        try {
            // This method should verify the reCAPTCHA response
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "reCAPTCHA verification failed: " + e.getMessage());
            response.setContentType("application/json");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            // Use SQL to fetch email and password only
            String userQuery = "SELECT email, password FROM employees WHERE email = ?";
            PreparedStatement userStatement = conn.prepareStatement(userQuery);
            userStatement.setString(1, email);
            ResultSet userRs = userStatement.executeQuery();

            if (userRs.next()) {
                String correctPassword = userRs.getString("password");

                // Verify the provided password using a secure method
                if (new StrongPasswordEncryptor().checkPassword(password, correctPassword)) {
                    // Password matches, set session attributes
                    request.getSession().setAttribute("userEmail", email); // Store the employee's email in the session
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Login successful.");
                } else {
                    // Incorrect password
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Incorrect password.");
                }
            } else {
                // Email not found in the database
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Email does not exist.");
            }

            // Close resources
            userRs.close();
            userStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "An error occurred: " + e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }
}
