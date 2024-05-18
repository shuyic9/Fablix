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

    private int distanceThreshold(String query) {
        if (query.length() <= 4) {
            return 1;
        } else if (query.length() <= 8) {
            return 2;
        } else {
            return 3;
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
        String pageParam = request.getParameter("page");
        String numResultsParam = request.getParameter("numResults");
//        System.out.println("numResultsParam: " + numResultsParam);
        String sortParam = request.getParameter("sort");
        String firstChar = request.getParameter("firstChar");

        int page = pageParam != null ? Integer.parseInt(pageParam) : 1;
        int numResults = numResultsParam != null ? Integer.parseInt(numResultsParam) : 10;
        int offset = (page - 1) * numResults;

        try (Connection conn = dataSource.getConnection()) {

            StringBuilder queryBuilder = new StringBuilder(
                "SELECT m.id, m.title, m.year, m.director, " +
                "(SELECT GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') " +
                "FROM genres_in_movies gm JOIN genres g ON gm.genreId = g.id WHERE gm.movieId = m.id) AS genres, " +
                "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.name, '|', s.id) " +
                "ORDER BY (SELECT COUNT(*) FROM stars_in_movies sim WHERE sim.starId = s.id) DESC, s.name SEPARATOR ', ') " +
                "FROM stars_in_movies sm JOIN stars s ON sm.starId = s.id WHERE sm.movieId = m.id LIMIT 3) AS all_stars, " +
                "r.rating " +
                "FROM movies m " +
                "JOIN ratings r ON m.id = r.movieId"
            );

            ArrayList <String> conditions = new ArrayList<>();
            ArrayList <Object> parameters = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                int nameThreshold = distanceThreshold(name);
                conditions.add("m.title LIKE ? OR edth(m.title, ?, ?)");
                parameters.add("%" + name + "%");
                parameters.add(name);
                parameters.add(nameThreshold);
            }

            if (year != null && !year.isEmpty()) {
                conditions.add("m.year = ?");
                parameters.add(year);
            }

            if (director != null && !director.isEmpty()) {
                int directorThreshold = distanceThreshold(director);
                conditions.add("m.director LIKE ? OR edth(m.director, ?, ?)");
                parameters.add("%" + director + "%");
                parameters.add(director);
                parameters.add(directorThreshold);
            }

            if (genre != null && !genre.isEmpty()) {
                int genreThreshold = distanceThreshold(genre);
                conditions.add("EXISTS (SELECT 1 FROM genres_in_movies gm JOIN genres g ON gm.genreId = g.id WHERE gm.movieId = m.id AND (g.name LIKE ? OR edth(g.name, ?, ?)))");
                parameters.add("%" + genre + "%");
                parameters.add(genre);
                parameters.add(genreThreshold);
            }

            if (star != null && !star.isEmpty()) {
                int starThreshold = distanceThreshold(star);
                conditions.add("EXISTS (SELECT 1 FROM stars_in_movies sm JOIN stars s ON sm.starId = s.id WHERE sm.movieId = m.id AND (s.name LIKE ? OR edth(s.name, ?, ?)))");
                parameters.add("%" + star + "%");
                parameters.add(star);
                parameters.add(starThreshold);
            }

            if (firstChar != null && !firstChar.isEmpty()) {
                if (firstChar.equals("*")) {
                    conditions.add("m.title REGEXP '^[^0-9A-Za-z]'"); // non-alphabetical
                } else {
                    conditions.add("m.title LIKE ?");
                    parameters.add(firstChar + "%");
                }
            }

            if (!conditions.isEmpty()) {
                queryBuilder.append(" WHERE ");
                queryBuilder.append(String.join(" AND ", conditions));
            }

            if (sortParam != null && !sortParam.isEmpty()) {
                switch (sortParam) {
                    case "title_asc_rating_desc":
                        queryBuilder.append(" ORDER BY m.title ASC, r.rating DESC");
                        break;
                    case "title_asc_rating_asc":
                        queryBuilder.append(" ORDER BY m.title ASC, r.rating ASC");
                        break;
                    case "title_desc_rating_desc":
                        queryBuilder.append(" ORDER BY m.title DESC, r.rating DESC");
                        break;
                    case "title_desc_rating_asc":
                        queryBuilder.append(" ORDER BY m.title DESC, r.rating ASC");
                        break;
                    case "rating_desc_title_asc":
                        queryBuilder.append(" ORDER BY r.rating DESC, m.title ASC");
                        break;
                    case "rating_desc_title_desc":
                        queryBuilder.append(" ORDER BY r.rating DESC, m.title DESC");
                        break;
                    case "rating_asc_title_asc":
                        queryBuilder.append(" ORDER BY r.rating ASC, m.title ASC");
                        break;
                    case "rating_asc_title_desc":
                        queryBuilder.append(" ORDER BY r.rating ASC, m.title DESC");
                        break;
                }
            }

            // pagination
            queryBuilder.append(" LIMIT ? OFFSET ?");

            try (PreparedStatement statement = conn.prepareStatement(queryBuilder.toString())) {

                for (int i = 0; i < parameters.size(); i++) {
                    statement.setObject(i + 1, parameters.get(i));
                }

                statement.setInt(parameters.size() + 1, numResults);
                statement.setInt(parameters.size() + 2, offset);

                System.out.println("Executing query: " + statement.toString());

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

                    // Split and limit the genres and stars to the first three
                    String[] genreDetails = movie_genres != null ? movie_genres.split(", ") : new String[0];
                    String[] starDetails = allStars != null ? allStars.split(", ") : new String[0];

                    // Handling genres - fetch up to first three
                    ArrayList<String> topGenres = new ArrayList<>();
                    for (int i = 0; i < Math.min(genreDetails.length, 3); i++) {
                        topGenres.add(genreDetails[i]);
                    }

                    // Handling stars - fetch up to first three
                    ArrayList<String> topStars = new ArrayList<>();
                    for (int i = 0; i < Math.min(starDetails.length, 3); i++) {
                        topStars.add(starDetails[i]);
                    }

                    // Create a JsonObject based on the data we retrieve from rs
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_id", movie_id);
                    jsonObject.addProperty("movie_title", movie_title);
                    jsonObject.addProperty("movie_year", movie_year);
                    jsonObject.addProperty("movie_director", movie_director);
                    jsonObject.addProperty("movie_genres", String.join(", ", topGenres));
                    jsonObject.addProperty("movie_stars", String.join(", ", topStars));
                    jsonObject.addProperty("movie_rating", movie_rating);

                    jsonArray.add(jsonObject);
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
            // Log error message
            e.printStackTrace();

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
