package com.spendx.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL  = System.getenv().getOrDefault("SPENDX_DB_URL", "jdbc:mysql://localhost:3306/spendx?useSSL=false&serverTimezone=UTC");
    private static final String USER = System.getenv().getOrDefault("SPENDX_DB_USER", "root");
    private static final String PASS = System.getenv("SPENDX_DB_PASS");

    private static Connection connection;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
    }
}
