package org.example.interfaceDAO;
import java.sql.SQLException;

import org.example.model.TitoloBacheca;
import org.example.model.ToDo;
import java.util.List;

public interface ToDoDAOInterface {
    void insert(ToDo todo, String bachecaTitolo, String username) throws SQLException;
    void update(ToDo todo) throws SQLException;
    boolean delete(ToDo todo, String username) throws SQLException; // 👈 Metodo da aggiungere
    List<ToDo> getToDosByBachecaAndUser(String bachecaTitolo, String username) throws SQLException;
    ToDo getToDoById(int id) throws Exception;
    boolean rimuoviPartecipazione(String titolo, String autore, String username) throws SQLException;
    List<ToDo> getToDosCondivisiConUtente(String username) throws Exception;
    void aggiornaBacheca(ToDo todo, TitoloBacheca nuovaBacheca) throws SQLException;
    void aggiornaPosizioni(List<ToDo> todos) throws SQLException;
    void aggiornaListaUtenti(ToDo todo) throws Exception;
    boolean esisteToDoCondiviso(String titolo, String autore, String username);


}

