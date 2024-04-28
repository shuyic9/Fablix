import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            InitialContext initialContext = new InitialContext();
            dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("message", "GET method is not supported for payments. Please use POST.");
        response.getWriter().write(responseJsonObject.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String card = request.getParameter("card");
        String exp = request.getParameter("exp");

        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastname = ? AND expiration = ?;";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, card);
            statement.setString(2, fname);
            statement.setString(3, lname);
            statement.setString(4, exp);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                responseJsonObject.addProperty("status", "success");
            } else {
                responseJsonObject.addProperty("status", "fail");
            }
            rs.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", "An error occurred: " + e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }
}
