import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        HashMap<String, Integer> cartItems = (HashMap<String, Integer>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new HashMap<>();
        }

        JsonObject responseJsonObject = new JsonObject();
        JsonArray cartItemsJsonArray = new JsonArray();

        cartItems.forEach((movieId, quantity) -> {
            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("movieId", movieId);
            itemJson.addProperty("quantity", quantity);
            // Assume a random price for example purpose; in a real application, retrieve it from your database
            itemJson.addProperty("price", Math.random() * 10 + 5);  // Prices between $5 and $15
            cartItemsJsonArray.add(itemJson);
        });

        responseJsonObject.add("cartItems", cartItemsJsonArray);
        response.getWriter().write(responseJsonObject.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        HashMap<String, Integer> cartItems = (HashMap<String, Integer>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new HashMap<>();
        }

        String movieId = request.getParameter("movieId");
        String change = request.getParameter("change");  // "+1" for increase, "-1" for decrease
        String remove = request.getParameter("remove");  // "true" if the item should be removed

        if ("true".equals(remove) && cartItems.containsKey(movieId)) {
            cartItems.remove(movieId);
        } else if (change != null && cartItems.containsKey(movieId)) {
            int quantityChange = Integer.parseInt(change);
            int newQuantity = cartItems.get(movieId) + quantityChange;
            if (newQuantity > 0) {
                cartItems.put(movieId, newQuantity);
            } else {
                cartItems.remove(movieId);
            }
        } else if (movieId != null && !movieId.isEmpty() && !cartItems.containsKey(movieId)) {
            // Assuming validation against a database is successful and the movie exists
            cartItems.put(movieId, 1);  // Start with a quantity of 1
        }

        session.setAttribute("cartItems", cartItems);

        JsonObject responseJsonObject = constructJsonResponse("success", "Cart updated successfully!");
        response.getWriter().write(responseJsonObject.toString());
    }

    private JsonObject constructJsonResponse(String status, String message) {
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("status", status);
        responseJsonObject.addProperty("message", message);
        return responseJsonObject;
    }
}
