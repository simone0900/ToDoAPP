package org.example.interfaceDAO;

import org.example.model.User;
import java.sql.SQLException;
import java.util.List;


public interface UtenteDAOInterface {
    boolean registraUtente(String username, String password);
    boolean loginUtente(String username, String password);
    boolean checkLogin(String username, String password);
    List<String> getAllUsernamesExcept(String exclude) throws SQLException;
}


