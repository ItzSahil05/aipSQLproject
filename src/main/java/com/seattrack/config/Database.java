package com.seattrack.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException error) {
            throw new SQLException("MySQL JDBC driver is missing from the application.", error);
        }
        return DriverManager.getConnection(
                required("MYSQL_JDBC_URL"),
                required("MYSQL_DB_USER"),
                required("MYSQL_DB_PASSWORD"));
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is not set: " + name);
        }
        return value;
    }
}
