import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {

    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        getMetadata(response);
    }

    private void getMetadata(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonArray jsonArray = new JsonArray();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {

            while (rs.next()) {
                String tableName = rs.getString(1);
                try (Statement stmtColumns = conn.createStatement();
                     ResultSet rsColumns = stmtColumns.executeQuery("DESCRIBE " + tableName)) {

                    JsonArray columnData = new JsonArray();
                    while (rsColumns.next()) {
                        JsonObject colObj = new JsonObject();
                        colObj.addProperty("Field", rsColumns.getString("Field"));
                        colObj.addProperty("Type", rsColumns.getString("Type"));
                        columnData.add(colObj);
                    }

                    JsonObject tableObj = new JsonObject();
                    tableObj.addProperty("tableName", tableName);
                    tableObj.add("columns", columnData);
                    jsonArray.add(tableObj);
                }
            }

            out.write(jsonArray.toString());
        } catch (SQLException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
