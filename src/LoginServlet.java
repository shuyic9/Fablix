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

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init() {
        try {
            InitialContext initialContext = new InitialContext();
            dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("username");  // Using email as the username field
        String password = request.getParameter("password");

        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM customers WHERE email = ? AND password = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                // Login successful:
                int userId = rs.getInt("id");
                String userFullName = rs.getString("firstName") + " " + rs.getString("lastName");
                request.getSession().setAttribute("userId", userId); // Store user ID in session
                request.getSession().setAttribute("user", userFullName);
                // Store user's name in session
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Login successful.");
            } else {
                // Login failed
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid email or password.");
            }
            rs.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "An error occurred: " + e.getMessage());
        }

        response.getWriter().write(responseJsonObject.toString());
    }
}
