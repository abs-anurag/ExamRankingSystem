package test;

import database.DBConnection;
import database.DBConnection.ConnectionConfig;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Unit test suite for DBConnection environment variable resolution,
 * URL formatting, credential handling, and password masking.
 */
public class DBConnectionConfigTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static final String[] DB_PROPERTIES = {
        "MYSQLHOST", "MYSQLPORT", "MYSQLDATABASE", "MYSQLUSER", "MYSQLPASSWORD",
        "DB_URL", "MYSQL_URL", "DATABASE_URL",
        "DB_HOST", "DB_PORT", "DB_NAME", "DB_DATABASE", "DB_USERNAME", "DB_USER", "DB_PASSWORD",
        "DB_PARAMS", "MYSQL_JDBC_PARAMS"
    };

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      DBCONNECTION CONFIGURATION TEST SUITE     ");
        System.out.println("=================================================");

        // Backup system properties
        Properties originalProps = new Properties();
        originalProps.putAll(System.getProperties());

        try {
            testMissingConfigurationThrowsException();
            testMissingHostThrowsException();
            testMissingUserThrowsException();
            testMissingPasswordThrowsException();
            testRailwayEnvironmentVariables();
            testRailwayDefaultPortAndDatabaseFallback();
            testGenericDiscreteVariables();
            testGenericDbUrlWithCredentials();
            testRailwayMysqlUrlFormat();
            testPasswordMasking();
        } finally {
            // Restore original system properties
            clearDbProperties();
            for (String key : originalProps.stringPropertyNames()) {
                System.setProperty(key, originalProps.getProperty(key));
            }
        }

        System.out.println("\n=================================================");
        System.out.println("   DBCONNECTION TEST SUMMARY: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println("=================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void clearDbProperties() {
        for (String prop : DB_PROPERTIES) {
            System.clearProperty(prop);
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName + " - Expected TRUE, got FALSE");
            testsFailed++;
        }
    }

    private static void assertEquals(String expected, String actual, String testName) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName + " - Expected: '" + expected + "', Got: '" + actual + "'");
            testsFailed++;
        }
    }

    // -------------------------------------------------------------------------
    // Test 1: Missing all configuration
    // -------------------------------------------------------------------------
    private static void testMissingConfigurationThrowsException() {
        System.out.println("\n--- Testing Missing Configuration ---");
        clearDbProperties();
        try {
            DBConnection.resolveConfig();
            assertTrue(false, "Should throw SQLException when configuration is missing");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("Missing required database environment variables"),
                    "Exception message explains missing configuration");
            assertTrue(e.getMessage().contains("MYSQLHOST") && e.getMessage().contains("DB_URL"),
                    "Exception message lists supported Railway and generic variables");
        }
    }

    // -------------------------------------------------------------------------
    // Test 2: Missing host
    // -------------------------------------------------------------------------
    private static void testMissingHostThrowsException() {
        System.out.println("\n--- Testing Missing Host ---");
        clearDbProperties();
        System.setProperty("DB_USER", "root");
        System.setProperty("DB_PASSWORD", "secret");
        try {
            DBConnection.resolveConfig();
            assertTrue(false, "Should throw SQLException when host is missing");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("host environment variable"),
                    "Exception explains missing host variable");
        }
    }

    // -------------------------------------------------------------------------
    // Test 3: Missing user
    // -------------------------------------------------------------------------
    private static void testMissingUserThrowsException() {
        System.out.println("\n--- Testing Missing User ---");
        clearDbProperties();
        System.setProperty("DB_HOST", "localhost");
        System.setProperty("DB_PASSWORD", "secret");
        try {
            DBConnection.resolveConfig();
            assertTrue(false, "Should throw SQLException when user is missing");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("user environment variable"),
                    "Exception explains missing user variable");
        }
    }

    // -------------------------------------------------------------------------
    // Test 4: Missing password
    // -------------------------------------------------------------------------
    private static void testMissingPasswordThrowsException() {
        System.out.println("\n--- Testing Missing Password ---");
        clearDbProperties();
        System.setProperty("DB_HOST", "localhost");
        System.setProperty("DB_USER", "root");
        try {
            DBConnection.resolveConfig();
            assertTrue(false, "Should throw SQLException when password is missing");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("password environment variable"),
                    "Exception explains missing password variable");
        }
    }

    // -------------------------------------------------------------------------
    // Test 5: Railway Environment Variables
    // -------------------------------------------------------------------------
    private static void testRailwayEnvironmentVariables() {
        System.out.println("\n--- Testing Railway Environment Variables ---");
        clearDbProperties();
        System.setProperty("MYSQLHOST", "roundhouse.proxy.rlwy.net");
        System.setProperty("MYSQLPORT", "12345");
        System.setProperty("MYSQLDATABASE", "railway");
        System.setProperty("MYSQLUSER", "root");
        System.setProperty("MYSQLPASSWORD", "railwaysecretpass");

        try {
            ConnectionConfig config = DBConnection.resolveConfig();
            assertEquals("jdbc:mysql://roundhouse.proxy.rlwy.net:12345/railway?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    config.getUrl(), "Railway JDBC URL properly constructed");
            assertEquals("root", config.getUsername(), "Railway username properly resolved");
            assertEquals("railwaysecretpass", config.getPassword(), "Railway password properly resolved");
            assertTrue(!config.getSanitizedUrl().contains("railwaysecretpass"), "Sanitized URL does not expose password");
            assertTrue(!config.toString().contains("railwaysecretpass"), "config.toString() masks password");
        } catch (SQLException e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Test 6: Railway Default Port and Database Fallback
    // -------------------------------------------------------------------------
    private static void testRailwayDefaultPortAndDatabaseFallback() {
        System.out.println("\n--- Testing Railway Defaults (Port: 3306, DB: exam_ranking_system) ---");
        clearDbProperties();
        System.setProperty("MYSQLHOST", "mysql.railway.internal");
        System.setProperty("MYSQLUSER", "appuser");
        System.setProperty("MYSQLPASSWORD", "apppass");

        try {
            ConnectionConfig config = DBConnection.resolveConfig();
            assertEquals("jdbc:mysql://mysql.railway.internal:3306/exam_ranking_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    config.getUrl(), "Railway URL with default port and default db constructed");
            assertEquals("appuser", config.getUsername(), "Username resolved");
        } catch (SQLException e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Test 7: Generic Discrete Variables
    // -------------------------------------------------------------------------
    private static void testGenericDiscreteVariables() {
        System.out.println("\n--- Testing Generic Discrete Variables (DB_HOST, DB_PORT, etc.) ---");
        clearDbProperties();
        System.setProperty("DB_HOST", "localhost");
        System.setProperty("DB_PORT", "3307");
        System.setProperty("DB_NAME", "custom_exams");
        System.setProperty("DB_USER", "dbadmin");
        System.setProperty("DB_PASSWORD", "dbpass123");

        try {
            ConnectionConfig config = DBConnection.resolveConfig();
            assertEquals("jdbc:mysql://localhost:3307/custom_exams?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    config.getUrl(), "Generic discrete variables JDBC URL properly constructed");
            assertEquals("dbadmin", config.getUsername(), "Username resolved");
            assertEquals("dbpass123", config.getPassword(), "Password resolved");
        } catch (SQLException e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Test 8: Generic DB_URL with DB_USERNAME and DB_PASSWORD
    // -------------------------------------------------------------------------
    private static void testGenericDbUrlWithCredentials() {
        System.out.println("\n--- Testing Generic DB_URL ---");
        clearDbProperties();
        System.setProperty("DB_URL", "jdbc:mysql://localhost:3306/exam_ranking_system?useSSL=true&serverTimezone=UTC");
        System.setProperty("DB_USERNAME", "root");
        System.setProperty("DB_PASSWORD", "mypass");

        try {
            ConnectionConfig config = DBConnection.resolveConfig();
            assertEquals("jdbc:mysql://localhost:3306/exam_ranking_system?useSSL=true&serverTimezone=UTC",
                    config.getUrl(), "DB_URL preserved as configured");
            assertEquals("root", config.getUsername(), "DB_USERNAME resolved");
            assertEquals("mypass", config.getPassword(), "DB_PASSWORD resolved");
        } catch (SQLException e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Test 9: Railway MYSQL_URL (mysql:// URI format)
    // -------------------------------------------------------------------------
    private static void testRailwayMysqlUrlFormat() {
        System.out.println("\n--- Testing Railway MYSQL_URL format (mysql://user:pass@host:port/db) ---");
        clearDbProperties();
        System.setProperty("MYSQL_URL", "mysql://root:secretToken123@containers-us-west-1.railway.app:7890/railway");

        try {
            ConnectionConfig config = DBConnection.resolveConfig();
            assertEquals("jdbc:mysql://containers-us-west-1.railway.app:7890/railway?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    config.getUrl(), "mysql:// URI parsed and converted to JDBC URL");
            assertEquals("root", config.getUsername(), "Username extracted from URI userInfo");
            assertEquals("secretToken123", config.getPassword(), "Password extracted from URI userInfo");
            assertTrue(!config.getSanitizedUrl().contains("secretToken123"), "Password not in sanitized URL");
        } catch (SQLException e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Test 10: Password Masking
    // -------------------------------------------------------------------------
    private static void testPasswordMasking() {
        System.out.println("\n--- Testing Password Masking ---");
        String uriWithPassword = "mysql://root:superSecretPassword@roundhouse.proxy.rlwy.net:3306/railway";
        String maskedUri = DBConnection.maskSensitive(uriWithPassword);
        assertEquals("mysql://root:****@roundhouse.proxy.rlwy.net:3306/railway", maskedUri, "URI password masked");

        String jdbcWithPassword = "jdbc:mysql://localhost:3306/db?user=root&password=superSecretPassword&useSSL=false";
        String maskedJdbc = DBConnection.maskSensitive(jdbcWithPassword);
        assertEquals("jdbc:mysql://localhost:3306/db?user=root&password=****&useSSL=false", maskedJdbc, "Query parameter password masked");

        String safeMessage = "Connection failed: Communications link failure";
        assertEquals(safeMessage, DBConnection.maskSensitive(safeMessage), "Message without credentials left intact");
    }
}

