package org.example.dao;

import org.example.db.ConnessioneDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.example.interfaceDAO.UtenteDAOInterface;

public class UtenteDAO implements UtenteDAOInterface {

    @Override
    public boolean registraUtente(String username, String password) {
        String sql = "INSERT INTO utenti (username, password) VALUES (?, ?)";

        try (Connection conn = ConnessioneDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Errore durante la registrazione: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean loginUtente(String username, String password) {
        String sql = "SELECT * FROM utenti WHERE username = ? AND password = ?";

        try (Connection conn = ConnessioneDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Se trova una riga, utente valido

        } catch (SQLException e) {
            System.err.println("Errore durante il login: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean checkLogin(String username, String password) {
        return loginUtente(username, password);
    }

    @Override
    public List<String> getAllUsernamesExcept(String exclude) throws SQLException {
        List<String> utenti = new ArrayList<>();
        String sql = "SELECT username FROM utenti WHERE username <> ?";

        try (Connection conn = ConnessioneDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, exclude);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                utenti.add(rs.getString("username"));
            }
        }

        return utenti;
    }
}
