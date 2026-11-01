package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection provides centralized database connectivity using JDBC.
 * It manages connections to the MySQL database for the Online Examination Ranking System.
 *
 * Academic Project: 2nd Year B.Tech CSE - ADSA
 */
public class DBConnection {

    // Database configuration credentials
    private static final String URL = "jdbc:mysql://localhost:3306/exam_ranking_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "0000";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            // Explicitly load the MySQL JDBC Driver class
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL ERROR: MySQL JDBC Driver not found in classpath.");
            System.err.println("Ensure mysql-connector-j.jar is included in the classpath.");
        }
    }

    /**
     * Establishes and returns a new Connection to the MySQL database.
     * 
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Utility method to test database connectivity at application startup.
     *
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database Connection Failed: " + e.getMessage());
            return false;
        }
    }
}

