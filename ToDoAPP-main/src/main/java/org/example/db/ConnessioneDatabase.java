package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnessioneDatabase {

    private static final String URL = "jdbc:postgresql://localhost:5432/todoapp?currentSchema=public";
    private static final String USER = "postgres";
    private static final String PASSWORD = "maradona10";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL non trovato", e);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
