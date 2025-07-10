package org.example.dao;

import org.example.interfaceDAO.BachecaDAOInterface;
import org.example.model.Bacheca;
import org.example.model.TitoloBacheca;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BachecaDAO implements BachecaDAOInterface {

    private final Connection conn;

    public BachecaDAO(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void inserisciSeNonEsiste(TitoloBacheca titolo, String descrizione, String username) {
        String checkQuery = "SELECT COUNT(*) FROM bacheche WHERE bacheca_titolo = ? AND utente_username = ?";
        String insertQuery = "INSERT INTO bacheche (bacheca_titolo, descrizione, utente_username) VALUES (?, ?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, titolo.name());
            checkStmt.setString(2, username);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, titolo.name());
                        insertStmt.setString(2, descrizione);
                        insertStmt.setString(3, username);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore in inserisciSeNonEsiste: " + e.getMessage());
        }
    }

    @Override
    public List<Bacheca> getBachechePerUtente(String username) throws SQLException {
        List<Bacheca> lista = new ArrayList<>();
        String sql = "SELECT * FROM bacheche WHERE utente_username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TitoloBacheca titolo = TitoloBacheca.valueOf(rs.getString("bacheca_titolo"));
                String descrizione = rs.getString("descrizione");

                Bacheca b = new Bacheca(titolo, username);
                b.setDescrizione(descrizione);
                lista.add(b);
            }
        }
        return lista;
    }

    public boolean elimina(TitoloBacheca titolo, String username) {
        String deleteToDos = "DELETE FROM todo WHERE bacheca_titolo = ? AND username = ?";
        String deleteRichieste = "DELETE FROM richieste_todo WHERE titolo_bacheca = ? AND destinatario = ?";
        String deleteBacheca = "DELETE FROM bacheche WHERE bacheca_titolo = ? AND utente_username = ?";

        try (
                PreparedStatement psToDo = conn.prepareStatement(deleteToDos);
                PreparedStatement psRichieste = conn.prepareStatement(deleteRichieste);
                PreparedStatement psBacheca = conn.prepareStatement(deleteBacheca)
        ) {
            // 1. Elimina ToDo associati
            psToDo.setString(1, titolo.name());
            psToDo.setString(2, username);
            psToDo.executeUpdate();

            // 2. Elimina richieste collegate
            psRichieste.setString(1, titolo.name());
            psRichieste.setString(2, username);
            psRichieste.executeUpdate();

            // 3. Elimina la bacheca
            psBacheca.setString(1, titolo.name());
            psBacheca.setString(2, username);
            int rows = psBacheca.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Errore durante l'eliminazione della bacheca: " + e.getMessage());
            return false;
        }
    }
    @Override
    public void aggiornaDescrizione(TitoloBacheca titolo, String nuovaDescrizione, String username) throws SQLException {
        String sql = "UPDATE bacheche SET descrizione = ? WHERE bacheca_titolo = ? AND utente_username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuovaDescrizione);
            stmt.setString(2, titolo.name());
            stmt.setString(3, username);
            stmt.executeUpdate();
        }
    }



}