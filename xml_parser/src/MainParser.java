import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.sql.*;
import java.util.*;
import java.util.UUID;


class MainParser extends DefaultHandler {

    private Connection conn;
    private PreparedStatement stmt;
    private List<Movie> movies; // All the movies
    private Map<String, Integer> genreMap;
    private Movie currentMovie = null;
    private String currentDirector = null;
    private boolean bfid = false;
    private boolean bTitle = false;
    private boolean bYear = false;
    private boolean bDirector = false;
    private boolean bGenre = false;
    private FileWriter fileWriter;
    private PrintWriter errorWriter;

    private FileWriter genreWriter;
    private FileWriter genreMovieWriter;
    private FileWriter ratingsWriter;
    private static Set<String> uniqueGenres;

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb?allowLoadLocalInfile=true";
    private static final String USER = "mytestuser";
    private static final String PASS = "My6$Password";


    ArrayList<Movie> badMovies;

    public MainParser() throws IOException {
        fileWriter = new FileWriter("movies.csv", true);
        genreWriter = new FileWriter("genres.csv", false);
        genreMovieWriter = new FileWriter("genres_in_movies.csv", true);
        ratingsWriter = new FileWriter("ratings.csv", true);
        errorWriter = new PrintWriter("errors.log", "UTF-8");

        badMovies = new ArrayList<>();
        uniqueGenres = new HashSet<>();
        genreMap = new HashMap<>();
        movies = new ArrayList<>();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("film")) {
            currentMovie = new Movie();
            currentMovie.setDirector(currentDirector);
        } else if (qName.equalsIgnoreCase("fid")) {
            bfid = true;
        } else if (qName.equalsIgnoreCase("t")) {
            bTitle = true;
        } else if (qName.equalsIgnoreCase("year")) {
            bYear = true;
        } else if (qName.equalsIgnoreCase("dirname")) {
            bDirector = true;
        } else if (qName.equalsIgnoreCase("cat")) {
            bGenre = true;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (bDirector) {
            currentDirector = new String(ch, start, length);
            bDirector = false; // Reset the flag after capturing
        } else if (currentMovie != null) {
            if (bfid) {
                String id = new String(ch, start, length).trim();
                if (id.isEmpty()) {
                    // Assign a unique UUID in case ID is missing
                    currentMovie.setId("UUID-" + UUID.randomUUID().toString());
                } else {
                    currentMovie.setId(id);
                }
                bfid = false;
            } else if (bTitle) {
                String title = new String(ch, start, length);
                if (title.trim().isEmpty()) {
                    currentMovie.setTitle("");
                } else {
                    currentMovie.setTitle(title);
                }
                bTitle = false;
            } else if (bYear) {
                try {
                    int year = Integer.parseInt(new String(ch, start, length));
                    if (year <= 0) {
                        currentMovie.setYear(0);
                    } else {
                        currentMovie.setYear(year);
                    }
                } catch (NumberFormatException e) {
                    currentMovie.setYear(0);
                }
                bYear = false;
            } else if (bGenre) {
                String genresText = new String(ch, start, length).toLowerCase().trim();
                String[] genres = genresText.split("\\s+"); // Split genre text by whitespace
                for (String genre : genres) {
                    currentMovie.getGenres().add(genre);
                }
                bGenre = false;
            }
        }
    }


    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("film")) {
            try {
                if (currentMovie.getTitle().equals("") || currentMovie.getYear() == 0) {
                    badMovies.add(currentMovie);
                } else {
                    fileWriter.append(currentMovie.toCSVFormat());
                    fileWriter.append("\n");
                    for (String genre : currentMovie.getGenres()) {
                        if (uniqueGenres.add(genre)) { // Check and add the lowercase genre
                            genreWriter.append(genre + "\n");
                        }
//                        genreMovieWriter.append(genreMap.get(genre) + "," + currentMovie.getId() + "\n");
                    }
                    movies.add(currentMovie);
                }
            } catch (IOException e) {
                System.out.println("Error writing to file");
            }
        }
    }

    public void endDocument() {
        try {
            fileWriter.flush();
            fileWriter.close();

            genreWriter.flush();
            genreWriter.close();

            loadDataIntoDatabase();
            fetchGenreIDs();

            System.out.println("Writing genres_in_movies.csv");
            writeGenresInMovies();
            System.out.println("Finished writing genres_in_movies.csv");

            genreMovieWriter.flush();
            genreMovieWriter.close();

            loadGenres_In_Movies();

            System.out.println("Writing ratings.csv");
            writeRatingsInMovies();
            System.out.println("Finished writing ratings.csv");

            ratingsWriter.flush();
            ratingsWriter.close();

            loadRatingsIntoDatabase();

            errorWriter.println("Total number of Bad movies: " + badMovies.size());
            for (Movie movie : badMovies) {
                errorWriter.println(movie.toString());
            }

            errorWriter.flush();
            errorWriter.close();

            // load data into database
//            loadDataIntoDatabase();

        } catch (IOException e) {
            System.out.println("Error closing file");
        }
    }

    private void writeGenresInMovies() {
        try {
            for (Movie movie : movies) {
                for (String genre : movie.getGenres()) {
                    if (genreMap.containsKey(genre)) {
                        genreMovieWriter.append(genreMap.get(genre) + "," + movie.getId() + "\n");
                    } else {
                        // Log error or handle the case where genre is not found
                        System.err.println("Genre not found in map: " + genre);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing genres in movies: " + e.getMessage());
        }
    }

    private void writeRatingsInMovies() {
        try {
            for (Movie movie : movies) {
                ratingsWriter.append(movie.getId() + "," + 0.0 + "," + 0 + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing ratings.csv: " + e.getMessage());
        }
    }

    public void loadDataIntoDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // Get the current working directory
            String currentDir = System.getProperty("user.dir");

            String moviesFilePath = currentDir + "/movies.csv";
            String genresFilePath = currentDir + "/genres.csv";

            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            String loadMovies = "LOAD DATA LOCAL INFILE '" + moviesFilePath.replace("\\", "\\\\") + "' INTO TABLE movies " +
                    "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' " +
                    "LINES TERMINATED BY '\\n' IGNORE 1 LINES " +
                    "(id, title, year, director);";

            String loadGenres = "LOAD DATA LOCAL INFILE '" + genresFilePath.replace("\\", "\\\\") + "' INTO TABLE genres " +
                    "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' " +
                    "(name);";


            System.out.println("Loading data into movies table...");
            stmt.execute(loadMovies);

            System.out.println("Loading data into genres table...");
            stmt.execute(loadGenres);

            System.out.println("Data loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception se) {
                se.printStackTrace();
            }
        }
    }

    private void fetchGenreIDs() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM genres")) {
            while (rs.next()) {
                // Key: genre name, Value: genre ID
                genreMap.put(rs.getString("name"), rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    private void loadGenres_In_Movies() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // Get the current working directory
            String currentDir = System.getProperty("user.dir");

            String genresInMoviesPath = currentDir + "/genres_in_movies.csv";

            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            String loadGenresInMovies = "LOAD DATA LOCAL INFILE '" + genresInMoviesPath.replace("\\", "\\\\") + "' INTO TABLE genres_in_movies " +
                    "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' " +
                    "(genreId, movieId);";


            System.out.println("Loading data into genres_in_movies table...");
            stmt.execute(loadGenresInMovies);

            System.out.println("Data loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception se) {
                se.printStackTrace();
            }
        }
    }

    public void loadRatingsIntoDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // Get the current working directory
            String currentDir = System.getProperty("user.dir");

            String ratingsPath = currentDir + "/ratings.csv";

            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            String loadRatings = "LOAD DATA LOCAL INFILE '" + ratingsPath.replace("\\", "\\\\") + "' INTO TABLE ratings " +
                    "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' " +
                    "(movieId, rating, numVotes);";


            System.out.println("Loading data into ratings table...");
            stmt.execute(loadRatings);

            System.out.println("Data loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception se) {
                se.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            String currentDir = System.getProperty("user.dir");

            String stanfordPath = currentDir + "/stanford-movies";
            String mainsPath = stanfordPath + "/mains243.xml";

            saxParser.parse(mainsPath, new MainParser());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



