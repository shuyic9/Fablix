import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;

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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        HashMap<String, Integer> cartItems = (HashMap<String, Integer>) session.getAttribute("cartItems");

        if (cartItems == null || cartItems.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"fail\",\"message\":\"Your cart is empty.\"}");
            return;
        }

        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String card = request.getParameter("card");
        String exp = request.getParameter("exp");
        int totalPrice = cartItems.values().stream().mapToInt(quantity -> quantity * 7).sum();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("totalPrice", totalPrice);

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
                responseJsonObject.addProperty("message", "Payment processed successfully.");
            } else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid payment details or card not found.");
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
