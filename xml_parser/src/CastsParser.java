import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class CastsParser extends DefaultHandler {

    private boolean bF = false; // movieId
    private boolean bA = false; // starId
    private String currentMovieId = "";
    private String currentStarName = "";
    private FileWriter fileWriter;
    private PrintWriter errorWriter;
    private Set<String> validMovieIds;
    private HashMap<String, String> validStarNames;
    private int errorCount = 0;

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb?allowLoadLocalInfile=true";
    private static final String USER = "mytestuser";
    private static final String PASS = "My6$Password";

    public CastsParser(Set<String> validMovieIds, HashMap<String, String> validStarNames) throws IOException {
        this.validMovieIds = validMovieIds;
        this.validStarNames = validStarNames;
        fileWriter = new FileWriter("stars_in_movies.csv", true);
        errorWriter = new PrintWriter(new FileWriter("errors.log", true));
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("m")) {
            // Reset for new movie-star record
            currentMovieId = "";
            currentStarName = "";
        } else if (qName.equalsIgnoreCase("f")) {
            bF = true;
        } else if (qName.equalsIgnoreCase("a")) {
            bA = true;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (bF) {
            currentMovieId = new String(ch, start, length).trim();
            bF = false;
        } else if (bA) {
            currentStarName = new String(ch, start, length).trim();
            bA = false;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("m")) {
            // Write to CSV file or log error
            try {
                if (!currentMovieId.isEmpty() && !currentStarName.isEmpty() && validMovieIds.contains(currentMovieId) && validStarNames.containsKey(currentStarName)) {
                    fileWriter.write(validStarNames.get(currentStarName) + "," + currentMovieId + "\n");
                } else {
                    errorWriter.println("MovieId: " + currentMovieId + " StarName: " + currentStarName);
                    errorCount++;
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

            loadStarsInMovies();

            errorWriter.println("\n\nTotal number of Sub Stars in Movie-Star Relation: " + errorCount);
            errorWriter.flush();
            errorWriter.close();
        } catch (IOException e) {
            System.out.println("Error closing file");
        }
    }

    private void loadStarsInMovies() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // Get the current working directory
            String currentDir = System.getProperty("user.dir");

            String starsInMoviesPath = currentDir + "/stars_in_movies.csv";

            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            String loadStars = "LOAD DATA LOCAL INFILE '" + starsInMoviesPath.replace("\\", "\\\\") + "' INTO TABLE stars_in_movies " +
                    "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' " +
                    "(starId, movieId);";


            System.out.println("Loading data into stars_in_movies table...");
            stmt.execute(loadStars);

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
            Set<String> validMovieIds = fetchMovieIds();
            HashMap<String, String> validStarNames = fetchStarNames();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            String currentDir = System.getProperty("user.dir");

            String stanfordPath = currentDir + "/stanford-movies";
            String castsPath = stanfordPath + "/casts124.xml";

            saxParser.parse(castsPath, new CastsParser(validMovieIds, validStarNames));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Set<String> fetchMovieIds() {
        Set<String> ids = new HashSet<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM movies")) {
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    private static HashMap<String, String> fetchStarNames() {
        HashMap<String, String> names = new HashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM stars")) {
            while (rs.next()) {
                // Key: name, Value: id
                names.put(rs.getString("name"), rs.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return names;
    }
}
