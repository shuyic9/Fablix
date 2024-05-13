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
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;

class ActorsParser extends DefaultHandler {

    private Connection conn;
    private PreparedStatement stmt;

    private Star currentStar = null;
    private boolean bStageName = false;
    private boolean bDob = false;
    private FileWriter fileWriter;
    private PrintWriter errorWriter;

    private int starCount = 0;
    ArrayList<Star> badStars;

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb?allowLoadLocalInfile=true";
    private static final String USER = "mytestuser";
    private static final String PASS = "My6$Password";

    public ActorsParser() throws IOException {
        fileWriter = new FileWriter("stars.csv", true);
        errorWriter = new PrintWriter(new FileWriter("errors.log", true));

        badStars = new ArrayList<>();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            currentStar = new Star();
            currentStar.setId("cnm" + starCount);
            starCount++;
        } else if (qName.equalsIgnoreCase("stagename")) {
            bStageName = true;
        } else if (qName.equalsIgnoreCase("dob")) {
            bDob = true;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (bStageName) {
            String stageName = new String(ch, start, length);
            if (stageName.trim().isEmpty()) {
                currentStar.setName("");
            } else {
                currentStar.setName(stageName);
            }
            bStageName = false;
        } else if (bDob) {
            try {
                int year = Integer.parseInt(new String(ch, start, length));
                if (year <= 0) {
                    currentStar.setBirthYear(0);
                } else {
                    currentStar.setBirthYear(year);
                }
            } catch (NumberFormatException e) {
                currentStar.setBirthYear(0);
            }
            bDob = false;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            try {
                if (currentStar.getName().equals("") || currentStar.getBirthYear() == 0) {
                    badStars.add(currentStar);
                } else {
                    fileWriter.append(currentStar.toCSVFormat());
                    fileWriter.append("\n");
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

            loadStars();

            errorWriter.println("\n\nTotal number of Invalid Stars: " + badStars.size());

            for (Star star : badStars) {
                errorWriter.println(star.toString());
            }

            errorWriter.flush();
            errorWriter.close();
        } catch (IOException e) {
            System.out.println("Error closing file");
        }
    }

    private void loadStars() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // Get the current working directory
            String currentDir = System.getProperty("user.dir");

            String starsPath = currentDir + "/stars.csv";

            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            String loadStars = "LOAD DATA LOCAL INFILE '" + starsPath.replace("\\", "\\\\") + "' INTO TABLE stars " +
                    "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' " +
                    "(id, name, birthYear);";


            System.out.println("Loading data into stars table...");
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
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            String currentDir = System.getProperty("user.dir");

            String stanfordPath = currentDir + "/stanford-movies";
            String actorsPath = stanfordPath + "/actors63.xml";

            saxParser.parse(actorsPath, new ActorsParser());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
