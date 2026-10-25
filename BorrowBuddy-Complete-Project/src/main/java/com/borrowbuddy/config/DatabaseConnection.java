package com.borrowbuddy.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager.
 * Loads connection properties from db.properties on the classpath.
 */
public class DatabaseConnection {

    private static final Properties props = new Properties();
    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;
    private static boolean initialized = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC Driver not found: " + e.getMessage());
        }
        loadProperties();
    }

    private DatabaseConnection() {}

    private static void loadProperties() {
        try (InputStream in = DatabaseConnection.class
                .getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException(
                    "db.properties not found on classpath. " +
                    "Copy src/main/resources/db.properties.example to db.properties and configure it.");
            }
            props.load(in);
            String host     = props.getProperty("db.host", "localhost");
            String port     = props.getProperty("db.port", "3306");
            String name     = props.getProperty("db.name", "borrowbuddy_db");
            dbUser          = props.getProperty("db.user", "root");
            dbPassword      = props.getProperty("db.password", "");
            jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
                host, port, name);
            initialized = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a new JDBC connection from the pool.
     *
     * @return open {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadProperties();
        }
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    /**
     * Safely closes a connection, suppressing any SQLException.
     *
     * @param conn the connection to close (may be null)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Warning: failed to close connection: " + e.getMessage());
            }
        }
    }

    /**
     * Tests whether the database is reachable.
     *
     * @return true if a test connection succeeds
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
