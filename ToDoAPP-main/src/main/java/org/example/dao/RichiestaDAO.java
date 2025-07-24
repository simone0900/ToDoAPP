package org.example.dao;

import org.example.interfaceDAO.RichiestaDAOInterface;
import org.example.model.Richiesta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RichiestaDAO implements RichiestaDAOInterface {
    private final Connection conn;

    public RichiestaDAO(Connection conn) {
        this.conn = conn;
    }


    @Override
    public void inserisciRichiesta(Richiesta richiesta) {
        String sql = "INSERT INTO richieste_todo (id_todo, mittente, destinatario, titolo_bacheca) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, richiesta.getIdToDo());
            ps.setString(2, richiesta.getMittente());
            ps.setString(3, richiesta.getDestinatario());
            ps.setString(4, richiesta.getTitoloBacheca());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante inserimento richiesta: " + e.getMessage());
        }
    }

    @Override
    public boolean esisteRichiesta(int idTodo, String mittente, String destinatario) {
        String sql = "SELECT COUNT(*) FROM richieste_todo WHERE id_todo = ? AND mittente = ? AND destinatario = ? AND stato = 'in_attesa'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTodo);
            stmt.setString(2, mittente);
            stmt.setString(3, destinatario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void aggiornaStato(int richiestaId, String nuovoStato) {
        String sql = "UPDATE richieste_todo SET stato = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuovoStato);
            ps.setInt(2, richiestaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Richiesta> getRichiestePerUtente(String username) {
        List<Richiesta> richieste = new ArrayList<>();
        String sql = "SELECT * FROM richieste_todo WHERE destinatario = ? AND stato = 'in_attesa'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Richiesta r = new Richiesta(
                            rs.getInt("id"),
                            rs.getInt("id_todo"),
                            rs.getString("mittente"),
                            rs.getString("destinatario"),
                            rs.getString("titolo_bacheca"),
                            rs.getString("stato"),
                            rs.getTimestamp("data_richiesta")
                    );
                    richieste.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return richieste;
    }


    public void inserisciCondivisione(int todoId, String destinatario) throws SQLException {
        String sql = "INSERT INTO condivisioni (id_todo, condiviso_con) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, todoId);
            stmt.setString(2, destinatario);
            stmt.executeUpdate();
        }
    }

    @Override
    public void aggiornaStatoRichiesta(int idToDo, String destinatario, String stato) throws SQLException {
        String sql = "UPDATE richieste_todo SET stato = ? WHERE id_todo = ? AND destinatario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, stato);
            stmt.setInt(2, idToDo);
            stmt.setString(3, destinatario);
            stmt.executeUpdate();
        }
    }
    @Override

    public List<String> getUtentiCondivisi(String titolo, String autore) throws SQLException {
        List<String> utenti = new ArrayList<>();
        String sql = "SELECT destinatario FROM richieste_todo WHERE id_todo IN (" +
                "  SELECT id FROM todo WHERE titolo = ? AND autore = ?" +
                ") AND stato = 'accettata'";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, titolo);
            stmt.setString(2, autore);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    utenti.add(rs.getString("destinatario"));
                }
            }
        }

        // Aggiungi anche l'autore alla lista (se vuoi visualizzarlo)
        if (!utenti.contains(autore)) {
            utenti.add(autore);
        }

        return utenti;
    }



}
