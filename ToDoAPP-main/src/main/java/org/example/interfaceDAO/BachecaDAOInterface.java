package org.example.interfaceDAO;

import org.example.model.Bacheca;
import org.example.model.TitoloBacheca;

import java.sql.SQLException;
import java.util.List;

public interface BachecaDAOInterface {

    void inserisciSeNonEsiste(TitoloBacheca titolo, String descrizione, String username) throws SQLException;

    List<Bacheca> getBachechePerUtente(String username) throws SQLException;

    boolean elimina(TitoloBacheca titolo, String username) throws SQLException;

    void aggiornaDescrizione(TitoloBacheca titolo, String nuovaDescrizione, String username) throws SQLException;

}


