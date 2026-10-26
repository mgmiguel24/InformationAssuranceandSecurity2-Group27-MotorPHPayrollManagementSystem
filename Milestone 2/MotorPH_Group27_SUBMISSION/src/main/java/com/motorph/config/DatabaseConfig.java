package com.motorph.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * CONTROL #2 (part 2): Externalized Configuration Vault.
 *
 * Singleton that is the ONLY class allowed to read database credentials.
 * The .env file is read exactly once, inside the static initialization block,
 * when the class is first loaded at application startup. Credentials are kept
 * private here and never passed to the service or controller layers.
 */
public final class DatabaseConfig {

    private static final DatabaseConfig INSTANCE;
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;
    private static final int APP_PORT;

    static {
        // Runs ONE time only. Real environment variables override the .env file.
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        DB_URL = require(dotenv, "DB_URL");
        DB_USER = require(dotenv, "DB_USER");
        DB_PASSWORD = require(dotenv, "DB_PASSWORD");
        APP_PORT = Integer.parseInt(dotenv.get("APP_PORT", "8080"));
        INSTANCE = new DatabaseConfig();
        System.out.println("[DatabaseConfig] Configuration loaded once at startup.");
    }

    private DatabaseConfig() { }

    public static DatabaseConfig getInstance() {
        return INSTANCE;
    }

    /** Hands out a live connection. The password itself never leaves this class. */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public int getAppPort() {
        return APP_PORT;
    }

    private static String require(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config value: " + key
                    + " (copy .env.example to .env and fill it in)");
        }
        return value;
    }
}
