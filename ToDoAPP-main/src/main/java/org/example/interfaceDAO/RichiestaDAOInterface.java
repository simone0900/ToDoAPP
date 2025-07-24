package org.example.interfaceDAO;

import org.example.model.Richiesta;

import java.sql.SQLException;
import java.util.List;

public interface RichiestaDAOInterface {
    void inserisciRichiesta(Richiesta richiesta);
    boolean esisteRichiesta(int idTodo, String mittente, String destinatario);
    void aggiornaStato(int richiestaId, String nuovoStato);
    List<Richiesta> getRichiestePerUtente(String username);
    List<String> getUtentiCondivisi(String titolo, String autore) throws SQLException;

    void aggiornaStatoRichiesta(int idToDo, String destinatario, String stato) throws SQLException;


// 👈 aggiunto
}
