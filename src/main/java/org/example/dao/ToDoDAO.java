package org.example.dao;

import org.example.controller.Controller;
import org.example.db.ConnessioneDatabase;
import org.example.model.TitoloBacheca;
import org.example.model.ToDo;
import org.example.interfaceDAO.ToDoDAOInterface;

import javax.swing.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class ToDoDAO implements ToDoDAOInterface {

    private final Connection conn;

    public ToDoDAO(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void update(ToDo todo) throws SQLException {
        // Recupera l’autore e la lista_utenti originali dal DB
        String autoreOriginale = null;
        String listaUtentiOriginale = null;

        String selectSql = "SELECT autore, lista_utenti FROM todo WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, todo.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    autoreOriginale = rs.getString("autore");
                    listaUtentiOriginale = rs.getString("lista_utenti");
                }
            }
        }

        // Se l'utente loggato NON è l'autore, preserva i valori originali
        String utenteCorrente = Controller.getUtenteLoggato();
        boolean isAutore = utenteCorrente != null && utenteCorrente.equals(autoreOriginale);

        if (!isAutore) {
            todo.setAutore(autoreOriginale); // NON modificare
            if (listaUtentiOriginale != null) {
                todo.setListaUtenti(Arrays.asList(listaUtentiOriginale.split(",")));
            } else {
                todo.setListaUtenti(null);
            }
        }

        // Esegui l’update
        String updateSql = "UPDATE todo SET titolo = ?, descrizione = ?, scadenza = ?, url = ?, colore_sfondo = ?, immagine_path = ?, lista_utenti = ?, autore = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, todo.getTitolo());
            ps.setString(2, todo.getDescrizione());
            ps.setDate(3, new java.sql.Date(todo.getScadenza().getTime()));
            ps.setString(4, todo.getUrl());
            ps.setString(5, todo.getColoreSfondo());
            ps.setString(6, todo.getImmaginePath());

            // Mantieni lista utenti solo se sei autore, altrimenti è già preservata
            List<String> utenti = todo.getListaUtenti();
            String utentiConcat = (utenti != null && !utenti.isEmpty()) ? String.join(",", utenti) : null;
            ps.setString(7, utentiConcat);

            // Autore
            ps.setString(8, todo.getAutore());

            // ID
            ps.setInt(9, todo.getId());

            ps.executeUpdate();
        }
    }


    @Override
    public void insert(ToDo todo, String titoloBacheca, String username) {
        String sql = "INSERT INTO todo (titolo, descrizione, scadenza, url, colore_sfondo, immagine_path, bacheca_titolo, username, autore, lista_utenti) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, todo.getTitolo());
            stmt.setString(2, todo.getDescrizione());
            stmt.setDate(3, new java.sql.Date(todo.getScadenza().getTime()));
            stmt.setString(4, todo.getUrl());
            stmt.setString(5, todo.getColoreSfondo());
            stmt.setString(6, todo.getImmaginePath());
            stmt.setString(7, titoloBacheca);
            stmt.setString(8, username); // Chi riceve il ToDo

            // Usa l’autore esistente del ToDo senza modificarlo
            stmt.setString(9, todo.getAutore());

            String utentiConcat = todo.getListaUtenti() != null ? String.join(",", todo.getListaUtenti()) : "";
            stmt.setString(10, utentiConcat.isBlank() ? null : utentiConcat);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    todo.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento del ToDo: " + e.getMessage());
        }
    }


    @Override
    public boolean delete(ToDo todo, String username) {
        if (todo.getBachecaTitolo() == null) {
            System.err.println("Errore: bachecaTitolo non impostato nel ToDo, impossibile eliminare.");
            return false;
        }

        String sql = "DELETE FROM todo WHERE titolo = ? AND bacheca_titolo = ? AND username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todo.getTitolo());
            stmt.setString(2, todo.getBachecaTitolo().name());
            stmt.setString(3, username);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Errore durante la cancellazione del ToDo: " + e.getMessage());
            return false;
        }
    }

    @Override
    public ToDo getToDoById(int id) throws SQLException {
        String sql = "SELECT * FROM todo WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ToDo todo = new ToDo(
                            rs.getString("titolo"),
                            rs.getString("descrizione"),
                            rs.getDate("scadenza")
                    );

                    todo.setId(rs.getInt("id"));
                    todo.setUrl(rs.getString("url"));
                    todo.setColoreSfondo(rs.getString("colore_sfondo"));
                    todo.setImmaginePath(rs.getString("immagine_path"));
                    todo.setUsername(rs.getString("username"));
                    todo.setAutore(rs.getString("autore"));

                    // Bacheca come enum (con fallback)
                    String bachecaRaw = rs.getString("bacheca_titolo");
                    if (bachecaRaw != null) {
                        todo.setBachecaTitolo(TitoloBacheca.valueOf(bachecaRaw));
                    }

                    // Lista utenti se presente (separati da virgola)
                    String utentiStr = rs.getString("lista_utenti");
                    if (utentiStr != null && !utentiStr.isBlank()) {
                        List<String> utenti = Arrays.asList(utentiStr.split(","));
                        todo.setListaUtenti(utenti);
                    }

                    return todo;
                }
            }
        }
        return null;
    }


    @Override
    public void aggiornaListaUtenti(ToDo todo) throws Exception {
        String sql = "UPDATE todo SET lista_utenti = ? WHERE id = ?";

        String lista = (todo.getListaUtenti() != null && !todo.getListaUtenti().isEmpty())
                ? String.join(",", todo.getListaUtenti())
                : null;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (lista != null) {
                ps.setString(1, lista);
            } else {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setInt(2, todo.getId());
            ps.executeUpdate();
        }
    }

    public int getToDoIdByTitoloAutore(String titolo, String autore) throws SQLException {
        String sql = "SELECT id FROM todo WHERE titolo = ? AND autore = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, titolo);
            ps.setString(2, autore);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        throw new SQLException("ToDo non trovato per titolo/autore.");
    }




    @Override
    public List<ToDo> getToDosCondivisiConUtente(String username) {
        List<ToDo> condivisi = new ArrayList<>();
        String sql = "SELECT * FROM todo WHERE lista_utenti IS NOT NULL AND lista_utenti LIKE ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + username + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ToDo todo = new ToDo(
                            rs.getString("titolo"),
                            rs.getString("descrizione"),
                            rs.getDate("scadenza")
                    );
                    todo.setId(rs.getInt("id"));
                    todo.setBachecaTitolo(TitoloBacheca.valueOf(rs.getString("bacheca_titolo")));
                    todo.setUrl(rs.getString("url"));
                    todo.setImmaginePath(rs.getString("immagine_path"));
                    todo.setColoreSfondo(rs.getString("colore_sfondo"));
                    todo.setAutore(rs.getString("autore")); // chi l'ha creato
                    todo.setUsername(username); // chi lo ha ricevuto

                    // Aggiungi lista utenti
                    String lista = rs.getString("lista_utenti");
                    if (lista != null && !lista.isEmpty()) {
                        todo.setListaUtenti(Arrays.asList(lista.split(",")));
                    }

                    condivisi.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return condivisi;
    }

    @Override
    public boolean rimuoviPartecipazione(String titolo, String autore, String username) throws SQLException {
        String sql = "DELETE FROM todo WHERE titolo = ? AND autore = ? AND username = ?";
        String sqlCondiv = "DELETE FROM condivisioni WHERE condiviso_con = ? AND id_todo = ?";

        int rowsAffected = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, titolo);
            ps.setString(2, autore);
            ps.setString(3, username);
            rowsAffected = ps.executeUpdate();
        }

        // Elimina anche dalla tabella condivisioni
        try (PreparedStatement ps2 = conn.prepareStatement(sqlCondiv)) {
            ps2.setString(1, username);
            ps2.setInt(2, getToDoIdByTitoloAutore(titolo, autore)); // Metodo helper se non lo hai già
            ps2.executeUpdate();
        }

        return rowsAffected > 0;
    }






    @Override
    public List<ToDo> getToDosByBachecaAndUser(String bachecaTitolo, String username) {
        List<ToDo> todos = new ArrayList<>();
        String sql = "SELECT * FROM todo WHERE bacheca_titolo = ? AND username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bachecaTitolo);
            stmt.setString(2, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ToDo todo = new ToDo(
                            rs.getString("titolo"),
                            rs.getString("descrizione"),
                            new Date(rs.getDate("scadenza").getTime())
                    );
                    todo.setId(rs.getInt("id"));
                    todo.setUrl(rs.getString("url"));
                    todo.setColoreSfondo(rs.getString("colore_sfondo"));
                    todo.setImmaginePath(rs.getString("immagine_path"));
                    todo.setBachecaTitolo(TitoloBacheca.valueOf(rs.getString("bacheca_titolo")));
                    todo.setAutore(rs.getString("autore"));
                    todo.setPosizione(rs.getInt("posizione"));


                    todos.add(todo);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        todos.sort(Comparator.comparingInt(ToDo::getPosizione));

        return todos;
    }
    @Override
    public void aggiornaBacheca(ToDo todo, TitoloBacheca nuovaBacheca) throws SQLException {
        String sql = "UPDATE todo SET bacheca_titolo = ? WHERE id = ? AND username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuovaBacheca.name());
            stmt.setInt(2, todo.getId());
            stmt.setString(3, todo.getUsername());  // Utente corrente

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.err.println("⚠️ Nessuna riga aggiornata nel DB per ToDo ID " + todo.getId());
            }
        }

    }
    @Override
    public void aggiornaPosizioni(List<ToDo> todos) throws SQLException {
        String sql = "UPDATE todo SET posizione = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ToDo todo : todos) {
                ps.setInt(1, todo.getPosizione());
                ps.setInt(2, todo.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }


}