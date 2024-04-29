import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        Integer recentSalesCount = (Integer) session.getAttribute("recentSalesCount");
        PrintWriter out = response.getWriter();

        if (userId == null || recentSalesCount == null) {
            response.sendRedirect("login.html");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT s.id AS salesId, m.title, s.copies FROM sales s JOIN movies m ON s.movieId = m.id WHERE s.customerId = ? ORDER BY s.id DESC LIMIT ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, recentSalesCount);

            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                int salesId = rs.getInt("salesId");
                String title = rs.getString("title");
                int copies = rs.getInt("copies");

                // Print each row to the console
                System.out.println("Sales ID: " + salesId + ", Title: " + title + ", Copies: " + copies);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("salesId", salesId);
                jsonObject.addProperty("movie_title", title);
                jsonObject.addProperty("quantity", copies);
                jsonArray.add(jsonObject);
            }

            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.add("purchasedItems", jsonArray);

            rs.close();
            statement.close();

            out.write(responseJsonObject.toString());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            request.getServletContext().log("Error:", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}
