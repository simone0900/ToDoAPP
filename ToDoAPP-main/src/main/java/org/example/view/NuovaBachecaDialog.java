package org.example.view;

import org.example.controller.Controller;
import org.example.model.TitoloBacheca;

import javax.swing.*;
import java.awt.*;

public class NuovaBachecaDialog extends JDialog {
    private JComboBox<TitoloBacheca> comboTitolo;
    private JTextField txtDescrizione;
    private JButton btnCrea;
    private JButton btnAnnulla;

    public NuovaBachecaDialog(JFrame parent, Runnable onSuccess) {
        super(parent, "Crea nuova Bacheca", true);
        setLayout(new GridLayout(3, 2, 5, 5));

        add(new JLabel("Tipo Bacheca:"));
        comboTitolo = new JComboBox<>(TitoloBacheca.values());
        add(comboTitolo);

        add(new JLabel("Descrizione:"));
        txtDescrizione = new JTextField();
        add(txtDescrizione);

        btnCrea = new JButton("Crea");
        btnAnnulla = new JButton("Annulla");
        add(btnCrea);
        add(btnAnnulla);

        btnCrea.addActionListener(e -> {
            TitoloBacheca titolo = (TitoloBacheca) comboTitolo.getSelectedItem();
            String descrizione = txtDescrizione.getText().trim();
            if (descrizione.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Inserisci una descrizione valida.",
                        "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = Controller.creaBacheca(titolo, descrizione);
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "Hai già una bacheca di tipo “" + titolo + "”.",
                        "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            onSuccess.run();
            dispose();
        });

        btnAnnulla.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(parent);
    }
}
