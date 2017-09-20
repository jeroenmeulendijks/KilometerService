package nl.meul.kilometerservice;

import com.drew.lang.GeoLocation;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Database {
    final private DateFormat myDateFormat;
    
    public Database() {
        myDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
    }
    
    public List<RoutePoint> getRoutePoints() {
        List<RoutePoint> rp = new ArrayList<>();
        String sql = "SELECT * FROM images ORDER BY fileDate ASC;";

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
                    Statement stat = conn.createStatement();
                    ResultSet rs = stat.executeQuery(sql)) {

                while (rs.next()) {
                    rp.add(new RoutePoint(myDateFormat.parse(rs.getString("fileDate")),
                                          rs.getInt("milage"),
                                          new GeoLocation(rs.getDouble("latitude"), rs.getDouble("longitude"))));
                }
            }
        } catch (SQLException | ClassNotFoundException | ParseException e) {
            // No handling needed
            System.out.println(String.format("Failed to get from database: %s", e.getLocalizedMessage()));
        }
        
        return rp;
    }
    
    public void addRecord(String filename, Date date, GeoLocation location, int milage) {
        removeIfExists(date);

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
                    Statement stat = conn.createStatement()) {
                
                stat.executeUpdate("CREATE TABLE IF NOT EXISTS images (name, fileDate, latitude, longitude, milage);");
                PreparedStatement prep = conn.prepareStatement("INSERT INTO images VALUES (?, ?, ?, ?, ?);");

                prep.setString(1, filename);
                prep.setString(2, myDateFormat.format(date));
                prep.setDouble(3, location.getLatitude());
                prep.setDouble(4, location.getLongitude());
                prep.setInt(5, milage);
                prep.addBatch();

                conn.setAutoCommit(false);
                prep.executeBatch();
                conn.setAutoCommit(true);
            }
        } catch (SQLException | ClassNotFoundException e) {
            // No handling needed
            System.out.println(String.format("Failed to add to database: %s", e.getLocalizedMessage()));
        }
    }
    
    private void removeIfExists(Date date) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db")) {

                PreparedStatement prep = conn.prepareStatement("DELETE FROM images WHERE fileDate = ?;");

                prep.setString(1, myDateFormat.format(date));
                prep.addBatch();

                conn.setAutoCommit(false);
                prep.executeBatch();
                conn.setAutoCommit(true);
            }
        } catch (SQLException | ClassNotFoundException e) {
            // No handling needed
            System.out.println(String.format("Failed to add to database: %s", e.getLocalizedMessage()));
        }
    }

    public boolean recordExists(String filename) {
        boolean foundRecord = false;
        
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db")) {
                String query = "SELECT (count(*) > 0) AS found FROM images WHERE name LIKE ?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, filename);

                try (ResultSet rs = pst.executeQuery()) {
                    // Only expecting a single result
                    if (rs.next()) {
                        foundRecord = rs.getBoolean(1); // "found" column
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            // No handling needed
            System.out.println(String.format("Failed to check database: %s", e.getLocalizedMessage()));
        }
        
        return foundRecord;
    }
}
