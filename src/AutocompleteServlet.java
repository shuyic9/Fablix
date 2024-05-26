import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String query = request.getParameter("query");
        JsonArray jsonArray = new JsonArray();

        if (query == null || query.trim().isEmpty()) {
            out.write(jsonArray.toString());
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String sqlQuery = "SELECT id, title FROM movies WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE) LIMIT 10";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, query + "*");
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                jsonArray.add(generateJsonObject(movieId, movieTitle));
            }

            rs.close();
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("JSON Response: " + jsonArray.toString()); // Log the JSON response
        out.write(jsonArray.toString());
        out.close();
    }

    private static JsonObject generateJsonObject(String id, String title) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", title);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("id", id);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }
}
