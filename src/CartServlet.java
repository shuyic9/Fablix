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
        // No need for 'change' or 'remove' parameters if you always want to increment the quantity
        if (movieId != null && !movieId.isEmpty()) {
            cartItems.put(movieId, cartItems.getOrDefault(movieId, 0) + 1); // Increment quantity by one or add new with quantity 1
        }

        session.setAttribute("cartItems", cartItems);

        // Prepare and send response
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("status", "success");
        responseJsonObject.addProperty("message", "Cart updated successfully!");
        response.getWriter().write(responseJsonObject.toString());
    }


    private JsonObject constructJsonResponse(String status, String message) {
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("status", status);
        responseJsonObject.addProperty("message", message);
        return responseJsonObject;
    }
}
