import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {

    private DataSource dataSource;

    @Override
    public void init() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        synchronized (session) {
            HashMap<String, Integer> cartItems = (HashMap<String, Integer>) session.getAttribute("cartItems");
            if (cartItems == null) {
                cartItems = new HashMap<>();
                session.setAttribute("cartItems", cartItems);
            }

            JsonObject responseJsonObject = new JsonObject();
            JsonArray cartItemsJsonArray = new JsonArray();

            cartItems.forEach((movieId, quantity) -> {
                JsonObject itemJson = new JsonObject();
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement statement = conn.prepareStatement("SELECT title FROM movies WHERE id = ?");
                    statement.setString(1, movieId);
                    ResultSet rs = statement.executeQuery();
                    if (rs.next()) {
                        itemJson.addProperty("movieTitle", rs.getString("title"));
                        itemJson.addProperty("movieId", movieId);
                        itemJson.addProperty("quantity", quantity);
                        itemJson.addProperty("price", 7);  // Fixed price at $7
                        cartItemsJsonArray.add(itemJson);
                    }
                    rs.close();
                    statement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            responseJsonObject.add("cartItems", cartItemsJsonArray);
            response.getWriter().write(responseJsonObject.toString());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        synchronized (session) {
            HashMap<String, Integer> cartItems = (HashMap<String, Integer>) session.getAttribute("cartItems");
            if (cartItems == null) {
                cartItems = new HashMap<>();
                session.setAttribute("cartItems", cartItems);
            }

            String movieId = request.getParameter("movieId");
            String action = request.getParameter("action");
            int currentCount = cartItems.getOrDefault(movieId, 0);

            switch (action) {
                case "add":
                case "increase":
                    cartItems.put(movieId, currentCount + 1);
                    break;
                case "decrease":
                    if (currentCount > 1) {
                        cartItems.put(movieId, currentCount - 1);
                    } else {
                        cartItems.remove(movieId);
                    }
                    break;
                case "delete":
                    cartItems.remove(movieId);
                    break;
                default:
                    break;
            }

            // Fetch and send updated cart details
            JsonArray cartItemsJsonArray = new JsonArray();
            cartItems.forEach((id, quantity) -> {
                JsonObject itemJson = new JsonObject();
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement statement = conn.prepareStatement("SELECT title FROM movies WHERE id = ?");
                    statement.setString(1, id);
                    ResultSet rs = statement.executeQuery();
                    if (rs.next()) {
                        itemJson.addProperty("movieTitle", rs.getString("title"));
                        itemJson.addProperty("movieId", id);
                        itemJson.addProperty("quantity", quantity);
                        itemJson.addProperty("price", 7);  // Fixed price at $7
                        cartItemsJsonArray.add(itemJson);
                    }
                    rs.close();
                    statement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.add("cartItems", cartItemsJsonArray);
            responseJsonObject.addProperty("status", "success");
            response.getWriter().write(responseJsonObject.toString());
        }
    }
}
