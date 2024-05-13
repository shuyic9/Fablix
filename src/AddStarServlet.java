import com.google.gson.JsonObject;
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
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add_star")
public class AddStarServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String starName = request.getParameter("name");
        String birthYear = request.getParameter("birthYear");
        PrintWriter out = response.getWriter();

        if (starName == null || starName.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Star name is required.");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String newStarIdQuery = "SELECT CONCAT('nm', LPAD(COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1), 7, '0')) FROM stars;";
            String newStarId = null;

            try (PreparedStatement idStatement = conn.prepareStatement(newStarIdQuery);
                 ResultSet rs = idStatement.executeQuery()) {
                if (rs.next()) {
                    newStarId = rs.getString(1);
                } else {
                    throw new SQLException("Failed to generate new star ID.");
                }
            }

            String query = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?);";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, newStarId);
                statement.setString(2, starName);
                if (birthYear != null && !birthYear.trim().isEmpty()) {
                    statement.setInt(3, Integer.parseInt(birthYear));
                } else {
                    statement.setNull(3, java.sql.Types.INTEGER);
                }

                int rowsAffected = statement.executeUpdate();
                JsonObject responseJsonObject = new JsonObject();
                if (rowsAffected > 0) {
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Star added successfully with ID: " + newStarId);
                    responseJsonObject.addProperty("starId", newStarId);
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Failed to add the star.");
                }
                out.write(responseJsonObject.toString());
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (SQLException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}
