package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * DBConnection provides centralized database connectivity using JDBC.
 * It manages connections to MySQL database for both local development
 * and cloud deployment (e.g., Railway).
 *
 * Supported configuration modes:
 * 1. Railway MySQL variables:
 *    - MYSQLHOST
 *    - MYSQLPORT (defaults to 3306)
 *    - MYSQLDATABASE (defaults to exam_ranking_system)
 *    - MYSQLUSER
 *    - MYSQLPASSWORD
 *
 * 2. Generic database variables:
 *    - DB_URL (e.g., jdbc:mysql://localhost:3306/exam_ranking_system)
 *    - DB_USERNAME (or DB_USER)
 *    - DB_PASSWORD
 *
 * 3. Discrete generic variables:
 *    - DB_HOST
 *    - DB_PORT (defaults to 3306)
 *    - DB_NAME / DB_DATABASE (defaults to exam_ranking_system)
 *    - DB_USER / DB_USERNAME
 *    - DB_PASSWORD
 *
 * 4. Railway connection URL:
 *    - MYSQL_URL / DATABASE_URL (format: mysql://user:pass@host:port/dbname)
 *
 * 5. Optional local .env file (checked in current directory or user.dir)
 *
 * Academic Project: 2nd Year B.Tech CSE - ADSA
 */
public class DBConnection {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_DATABASE = "exam_ranking_system";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_PARAMS = "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final Map<String, String> ENV_FILE_CACHE = new HashMap<>();

    static {
        try {
            // Explicitly load the MySQL JDBC Driver class
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL ERROR: MySQL JDBC Driver not found in classpath.");
            System.err.println("Ensure mysql-connector-j.jar is included in the classpath.");
        }
        loadDotEnvFile();
    }

    /**
     * Loads key-value pairs from an optional local .env file in the current working directory
     * or project root, if present. Does not overwrite existing environment variables.
     */
    private static void loadDotEnvFile() {
        File[] candidates = new File[] {
            new File(".env"),
            new File(System.getProperty("user.dir", "."), ".env"),
            new File("../.env"),
            new File("../../.env")
        };
        for (File file : candidates) {
            if (file.exists() && file.isFile() && file.canRead()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String key = line.substring(0, eqIdx).trim();
                            String value = line.substring(eqIdx + 1).trim();
                            if ((value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) ||
                                (value.startsWith("'") && value.endsWith("'") && value.length() >= 2)) {
                                value = value.substring(1, value.length() - 1);
                            }
                            if (!ENV_FILE_CACHE.containsKey(key)) {
                                ENV_FILE_CACHE.put(key, value);
                            }
                        }
                    }
                    break;
                } catch (IOException ignored) {
                    // Ignore failures reading local .env file
                }
            }
        }
    }

    /**
     * Checks if a setting exists in environment variables, system properties, or local .env cache.
     */
    public static boolean hasEnvOrProperty(String key) {
        return (System.getenv(key) != null) ||
               (System.getProperty(key) != null) ||
               ENV_FILE_CACHE.containsKey(key);
    }

    /**
     * Retrieves a configuration setting by checking:
     * 1. OS Environment Variables (System.getenv)
     * 2. JVM System Properties (System.getProperty)
     * 3. Local .env file cache
     */
    public static String getEnvOrProperty(String key) {
        String val = System.getenv(key);
        if (val != null) {
            return val.trim();
        }
        val = System.getProperty(key);
        if (val != null) {
            return val.trim();
        }
        val = ENV_FILE_CACHE.get(key);
        if (val != null) {
            return val.trim();
        }
        return null;
    }

    /**
     * Holds resolved database connection parameters.
     */
    public static class ConnectionConfig {
        private final String url;
        private final String username;
        private final String password;

        public ConnectionConfig(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public String getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getSanitizedUrl() {
            return maskSensitive(url);
        }

        @Override
        public String toString() {
            return "ConnectionConfig{url='" + getSanitizedUrl() + "', username='" + username + "', password=****}";
        }
    }

    /**
     * Resolves the database connection configuration based on environment variables.
     *
     * @return ConnectionConfig containing the JDBC URL, username, and password
     * @throws SQLException if required configuration is missing or invalid
     */
    public static ConnectionConfig resolveConfig() throws SQLException {
        // 1. Check for full JDBC URL or URI
        String directUrl = getEnvOrProperty("DB_URL");
        if (directUrl == null || directUrl.isEmpty()) {
            directUrl = getEnvOrProperty("MYSQL_URL");
        }
        if (directUrl == null || directUrl.isEmpty()) {
            directUrl = getEnvOrProperty("DATABASE_URL");
        }

        if (directUrl != null && !directUrl.trim().isEmpty()) {
            directUrl = directUrl.trim();

            // Handle mysql:// format (often provided by Railway MYSQL_URL / DATABASE_URL)
            if (directUrl.startsWith("mysql://")) {
                try {
                    URI uri = new URI(directUrl);
                    String host = uri.getHost();
                    if (host == null || host.isEmpty()) {
                        throw new SQLException("Invalid host in database URL: " + maskSensitive(directUrl));
                    }
                    int port = uri.getPort() != -1 ? uri.getPort() : DEFAULT_PORT;
                    String path = uri.getPath();
                    String database = (path != null && path.length() > 1) ? path.substring(1) : DEFAULT_DATABASE;

                    String username = null;
                    String password = null;
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null) {
                        String[] parts = userInfo.split(":", 2);
                        username = parts[0];
                        if (parts.length > 1) {
                            password = parts[1];
                        }
                    }
                    if (username == null || username.isEmpty()) {
                        username = resolveUsername();
                    }
                    if (password == null) {
                        password = resolvePassword();
                    }

                    String queryParams = uri.getQuery();
                    if (queryParams == null || queryParams.isEmpty()) {
                        String customParams = getEnvOrProperty("DB_PARAMS");
                        if (customParams == null || customParams.isEmpty()) {
                            customParams = getEnvOrProperty("MYSQL_JDBC_PARAMS");
                        }
                        queryParams = (customParams != null && !customParams.isEmpty()) ? customParams : DEFAULT_PARAMS;
                    }

                    String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + queryParams;
                    return new ConnectionConfig(jdbcUrl, username, password);
                } catch (URISyntaxException e) {
                    throw new SQLException("Failed to parse database URI: " + maskSensitive(directUrl), e);
                }
            } else {
                // Direct JDBC URL
                String jdbcUrl = directUrl;
                if (!jdbcUrl.contains("?")) {
                    String customParams = getEnvOrProperty("DB_PARAMS");
                    if (customParams == null || customParams.isEmpty()) {
                        customParams = getEnvOrProperty("MYSQL_JDBC_PARAMS");
                    }
                    String params = (customParams != null && !customParams.isEmpty()) ? customParams : DEFAULT_PARAMS;
                    jdbcUrl += "?" + params;
                }
                String username = resolveUsername();
                String password = resolvePassword();
                return new ConnectionConfig(jdbcUrl, username, password);
            }
        }

        // 2. Check for discrete host/port/database variables
        String host = getEnvOrProperty("MYSQLHOST");
        if (host == null || host.isEmpty()) {
            host = getEnvOrProperty("DB_HOST");
        }

        String portStr = getEnvOrProperty("MYSQLPORT");
        if (portStr == null || portStr.isEmpty()) {
            portStr = getEnvOrProperty("DB_PORT");
        }

        String database = getEnvOrProperty("MYSQLDATABASE");
        if (database == null || database.isEmpty()) {
            database = getEnvOrProperty("DB_NAME");
        }
        if (database == null || database.isEmpty()) {
            database = getEnvOrProperty("DB_DATABASE");
        }

        String username = resolveUsername();
        String password = resolvePassword();

        // 3. Validate required configuration
        if ((host == null || host.isEmpty()) && (username == null || username.isEmpty()) && password == null) {
            throw new SQLException(
                "Database configuration error: Missing required database environment variables.\n" +
                "Please configure either Railway MySQL variables or generic database variables:\n" +
                "  Railway MySQL variables:\n" +
                "    - MYSQLHOST (e.g., roundhouse.proxy.rlwy.net or mysql.railway.internal)\n" +
                "    - MYSQLPORT (default: 3306)\n" +
                "    - MYSQLDATABASE (default: exam_ranking_system)\n" +
                "    - MYSQLUSER (e.g., root)\n" +
                "    - MYSQLPASSWORD (your MySQL password)\n" +
                "  Generic database variables:\n" +
                "    - DB_URL, DB_USERNAME, DB_PASSWORD\n" +
                "    - or DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD\n" +
                "Refer to README.md for setup instructions."
            );
        }

        if (host == null || host.isEmpty()) {
            throw new SQLException(
                "Database configuration error: Missing required database host environment variable.\n" +
                "Please set MYSQLHOST (for Railway) or DB_HOST (for local development, e.g., localhost)."
            );
        }

        if (username == null || username.isEmpty()) {
            throw new SQLException(
                "Database configuration error: Missing required database user environment variable.\n" +
                "Please set MYSQLUSER (for Railway) or DB_USERNAME / DB_USER."
            );
        }

        if (password == null) {
            throw new SQLException(
                "Database configuration error: Missing required database password environment variable.\n" +
                "Please set MYSQLPASSWORD (for Railway) or DB_PASSWORD."
            );
        }

        int port = DEFAULT_PORT;
        if (portStr != null && !portStr.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                throw new SQLException("Database configuration error: Invalid database port '" + portStr + "'. Must be a number.");
            }
        }

        if (database == null || database.trim().isEmpty()) {
            database = DEFAULT_DATABASE;
        }

        String customParams = getEnvOrProperty("DB_PARAMS");
        if (customParams == null || customParams.isEmpty()) {
            customParams = getEnvOrProperty("MYSQL_JDBC_PARAMS");
        }
        String params = (customParams != null && !customParams.isEmpty()) ? customParams : DEFAULT_PARAMS;

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + params;
        return new ConnectionConfig(jdbcUrl, username, password);
    }

    private static String resolveUsername() {
        String user = getEnvOrProperty("MYSQLUSER");
        if (user == null || user.isEmpty()) {
            user = getEnvOrProperty("DB_USERNAME");
        }
        if (user == null || user.isEmpty()) {
            user = getEnvOrProperty("DB_USER");
        }
        return (user != null && !user.isEmpty()) ? user : null;
    }

    private static String resolvePassword() {
        if (hasEnvOrProperty("MYSQLPASSWORD")) {
            return getEnvOrProperty("MYSQLPASSWORD");
        }
        if (hasEnvOrProperty("DB_PASSWORD")) {
            return getEnvOrProperty("DB_PASSWORD");
        }
        return null;
    }

    /**
     * Masks passwords in URLs and error messages to avoid leaking credentials in logs.
     *
     * @param text input string potentially containing credentials
     * @return sanitized string with passwords replaced by asterisks
     */
    public static String maskSensitive(String text) {
        if (text == null) return null;
        return text.replaceAll("(?i)(://[^:/@]+:)([^@]+)(@)", "$1****$3")
                   .replaceAll("(?i)(password=)[^&;\\s]*", "$1****");
    }

    /**
     * Establishes and returns a new Connection to the MySQL database.
     * 
     * @return Connection object
     * @throws SQLException if a database access error occurs or configuration is missing
     */
    public static Connection getConnection() throws SQLException {
        ConnectionConfig config = resolveConfig();
        if (config.getUsername() != null) {
            return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
        } else {
            return DriverManager.getConnection(config.getUrl());
        }
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
            System.err.println("Database Connection Failed: " + maskSensitive(e.getMessage()));
            return false;
        }
    }
}
