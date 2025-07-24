package org.example.view;

import org.example.controller.Controller;
import org.example.dao.UtenteDAO;
import org.example.interfaceDAO.UtenteDAOInterface;

import javax.swing.*;

public class LoginWindow {
    private JPanel panel;
    private JTextField textUsername;
    private JPasswordField textPassword;
    private JCheckBox mostraPasswordCheckBox;
    private JButton loginButton;
    private JButton registratiButton;

    public LoginWindow() {
        loginButton.addActionListener(e -> {
            String user = textUsername.getText().trim();
            String pass = new String(textPassword.getPassword()).trim();
            if (Controller.login(user, pass)) {
                JOptionPane.showMessageDialog(null, "Login riuscito!");
                Controller.showBachecaWindow();
                SwingUtilities.invokeLater(Controller::mostraDialogRichieste);
            } else {
                JOptionPane.showMessageDialog(null, "Credenziali errate.");
            }
        });

        mostraPasswordCheckBox.addActionListener(e -> {
            textPassword.setEchoChar(mostraPasswordCheckBox.isSelected() ? (char) 0 : '•');
        });

        registratiButton.addActionListener(e -> {
            String user = textUsername.getText().trim();
            String pass = new String(textPassword.getPassword()).trim();
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Inserisci username e password.");
                return;
            }
            UtenteDAOInterface utenteDAO = new UtenteDAO();
            if (utenteDAO.registraUtente(user, pass)) {
                JOptionPane.showMessageDialog(null, "Registrazione avvenuta con successo!");
            } else {
                JOptionPane.showMessageDialog(null, "Errore nella registrazione. Utente già esistente?");
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }
}
