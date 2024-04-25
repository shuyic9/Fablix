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
import java.sql.Statement;
import java.util.ArrayList;


// Declaring a WebServlet called MovieListServlet, which maps to url "/api/movies"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movies")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        String name = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String genre = request.getParameter("genre");

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            StringBuilder queryBuilder = new StringBuilder(
                "SELECT m.id, m.title, m.year, m.director, " +
                "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genres, " +
                "GROUP_CONCAT(DISTINCT CONCAT(s.name, '|', s.id) " +
                "ORDER BY (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) DESC, s.name SEPARATOR ', ') AS all_stars, " +
                "r.rating " +
                "FROM movies m " +
                "JOIN genres_in_movies gm ON m.id = gm.movieId " +
                "JOIN genres g ON gm.genreId = g.id " +
                "JOIN stars_in_movies sm ON m.id = sm.movieId " +
                "JOIN stars s ON sm.starId = s.id " +
                "JOIN ratings r ON m.id = r.movieId "
            );

            ArrayList <String> conditions = new ArrayList<>();
            ArrayList <Object> parameters = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                conditions.add("m.title LIKE ?");
                parameters.add("%" + name + "%");
            }
            if (year != null && !year.isEmpty()) {
                conditions.add("m.year = ?");
                parameters.add(year);
            }
            if (director != null && !director.isEmpty()) {
                conditions.add("m.director LIKE ?");
                parameters.add("%" + director + "%");
            }

            if (!conditions.isEmpty()) {
                queryBuilder.append(" WHERE ");
                queryBuilder.append(String.join(" AND ", conditions));
            }

            // only caches 100 queries, no ordering
            queryBuilder.append(" GROUP BY m.id LIMIT 100");

            try (PreparedStatement statement = conn.prepareStatement(queryBuilder.toString())) {

                for (int i = 0; i < parameters.size(); i++) {
                    statement.setObject(i + 1, parameters.get(i));
                }

                // Perform the query
                ResultSet rs = statement.executeQuery();

                JsonArray jsonArray = new JsonArray();

                // Iterate through each row of rs
                while (rs.next()) {
                    String movie_id = rs.getString("id");
                    String movie_title = rs.getString("title");
                    String movie_year = rs.getString("year");
                    String movie_director = rs.getString("director");
                    String movie_genres = rs.getString("genres");
//                    System.out.println(movie_genres);
                    String allStars = rs.getString("all_stars");
//                    System.out.println(allStars);
                    String movie_rating = rs.getString("rating");

                    String[] starDetails = allStars.split(",");
                    String[] genreDetails = movie_genres.split(",");
                    boolean starMatchFound = star.isEmpty() || allStars.toLowerCase().contains(star.toLowerCase());
                    boolean genreMatchFound = genre.isEmpty() || movie_genres.toLowerCase().contains(genre.toLowerCase());

                    // Handle the genre and star case separately
                    if (genreMatchFound && starMatchFound) {
                        ArrayList<String> displayedStars = new ArrayList<>();
                        ArrayList<String> displayedGenres = new ArrayList<>();

                        for (String singleGenre : genreDetails) {
                            if (displayedGenres.size() < 3) {
                                displayedGenres.add(singleGenre);
                            }
                        }

                        for (String singleStar : starDetails) {
                            if (displayedStars.size() < 3) {
                                displayedStars.add(singleStar);
                            }
                        }
                        String topThreeStars = String.join(", ", displayedStars);
                        String topThreeGenres = String.join(", ", displayedGenres);
//                        System.out.println(topThreeStars);

                        // Create a JsonObject based on the data we retrieve from rs
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("movie_id", movie_id);
                        jsonObject.addProperty("movie_title", movie_title);
                        jsonObject.addProperty("movie_year", movie_year);
                        jsonObject.addProperty("movie_director", movie_director);
                        jsonObject.addProperty("movie_genres", topThreeGenres);
                        jsonObject.addProperty("movie_stars", topThreeStars);
                        jsonObject.addProperty("movie_rating", movie_rating);

                        jsonArray.add(jsonObject);
                    }
                }
                rs.close();
                statement.close();

                // Log to localhost log
                request.getServletContext().log("getting " + jsonArray.size() + " results");

                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
