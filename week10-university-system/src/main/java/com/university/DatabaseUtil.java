package com.university;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseUtil {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/university_db?connectTimeout=5000&socketTimeout=10000";
    private static final Path CONFIG_FILE = Path.of(
            System.getProperty("user.home"), ".university-system", "database.properties");

    private DatabaseUtil() {
    }

    public static Connection getConnection() throws SQLException {
        Properties config = readConfig();
        String url = setting(config, "university.db.url", "UNIVERSITY_DB_URL", "url", DEFAULT_URL);
        String user = setting(config, "university.db.user", "UNIVERSITY_DB_USER", "user", null);
        String password = setting(config, "university.db.password", "UNIVERSITY_DB_PASSWORD", "password", "");

        if (url.isBlank()) {
            throw new SQLException("The database URL is empty. Check your database configuration.");
        }
        if (user == null || user.isBlank()) {
            throw new SQLException("Database credentials are not configured. Set UNIVERSITY_DB_USER and "
                    + "UNIVERSITY_DB_PASSWORD, or create " + CONFIG_FILE + ".");
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static Properties readConfig() throws SQLException {
        Properties config = new Properties();
        if (!Files.isRegularFile(CONFIG_FILE)) {
            return config;
        }

        try (InputStream input = Files.newInputStream(CONFIG_FILE)) {
            config.load(input);
            return config;
        } catch (IOException e) {
            throw new SQLException("Could not read database configuration at " + CONFIG_FILE, e);
        }
    }

    private static String setting(Properties config, String property, String environment,
                                  String key, String defaultValue) {
        String value = System.getProperty(property);
        if (value != null) {
            return value;
        }
        value = System.getenv(environment);
        if (value != null) {
            return value;
        }
        return config.getProperty(key, defaultValue);
    }
}
