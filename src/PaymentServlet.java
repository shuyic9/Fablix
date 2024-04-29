import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;

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
        Integer userId = (Integer) session.getAttribute("userId");
        HashMap<String, Integer> cartItems = (HashMap<String, Integer>) session.getAttribute("cartItems");

        if (userId == null || cartItems == null || cartItems.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"fail\",\"message\":\"Your cart is empty or user is not logged in.\"}");
            return;
        }

        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String card = request.getParameter("card");
        String exp = request.getParameter("exp");
        int totalPrice = cartItems.values().stream().mapToInt(quantity -> quantity * 7).sum(); // Assume price per item is $7

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

                // Record sales for each movie in the cart
                String salesQuery = "INSERT INTO sales (customerId, movieId, copies, saleDate) VALUES (?, ?, ?, ?)";
                PreparedStatement salesStatement = conn.prepareStatement(salesQuery);
                for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
                    salesStatement.setInt(1, userId);
                    salesStatement.setString(2, entry.getKey());
                    salesStatement.setInt(3, entry.getValue());
                    salesStatement.setDate(4, Date.valueOf(LocalDate.now()));
                    System.out.println("Preparing to insert sale record - Movie ID: " + entry.getKey() + ", Copies: " + entry.getValue());
                    salesStatement.executeUpdate();
                }
                salesStatement.close();

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
