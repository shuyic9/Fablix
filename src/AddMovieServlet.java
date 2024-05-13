import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add_movie")
public class AddMovieServlet extends HttpServlet {

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
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        String starBirthYear = request.getParameter("starBirthYear");
        String genreName = request.getParameter("genreName");

        // Debug print statements to check each parameter
        System.out.println("Received title: " + title);
        System.out.println("Received year: " + year);
        System.out.println("Received director: " + director);
        System.out.println("Received starName: " + starName);
        System.out.println("Received starBirthYear: " + starBirthYear);
        System.out.println("Received genreName: " + genreName);

        PrintWriter out = response.getWriter();

        if (title == null || title.trim().isEmpty() ||
                director == null || director.trim().isEmpty() ||
                starName == null || starName.trim().isEmpty() ||
                genreName == null || genreName.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String procedureCall = "{CALL add_movie(?, ?, ?, ?, ?, ?)}";
            try (CallableStatement stmt = conn.prepareCall(procedureCall)) {
                stmt.setString(1, title);
                if (year != null && !year.trim().isEmpty()) {
                    stmt.setInt(2, Integer.parseInt(year));
                } else {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                }
                stmt.setString(3, director);
                stmt.setString(4, starName);
                if (starBirthYear != null && !starBirthYear.trim().isEmpty()) {
                    stmt.setInt(5, Integer.parseInt(starBirthYear));
                } else {
                    stmt.setNull(5, java.sql.Types.INTEGER);
                }
                stmt.setString(6, genreName);

                boolean hasResultSet = stmt.execute();
                JsonObject responseJsonObject = new JsonObject();

                if (hasResultSet) {
                    try (var rs = stmt.getResultSet()) {
                        if (rs.next()) {
                            String message = rs.getString(1);
                            responseJsonObject.addProperty("status", "success");
                            responseJsonObject.addProperty("message", message);
                        } else {
                            responseJsonObject.addProperty("status", "fail");
                            responseJsonObject.addProperty("message", "Failed to add the movie.");
                        }
                    }
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Failed to add the movie.");
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
