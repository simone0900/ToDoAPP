package org.example.view;

import org.example.controller.Controller;
import org.example.dao.RichiestaDAO;
import org.example.dao.ToDoDAO;
import org.example.db.ConnessioneDatabase;
import org.example.model.Bacheca;
import org.example.model.TitoloBacheca;
import org.example.model.ToDo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class BachecaWindow {
    private JPanel mainPanel;
    private JPanel bachechePanel;
    private JButton logoutButton;
    private JButton createBachecaButton;
    private JTextField searchField;
    private JTextField dateField;
    private JButton searchButton;
    private JButton todayButton;
    private JPanel panelBachecheList;

    public BachecaWindow() {
        setupUI();
        setupActions();
        aggiornaListaBacheche(null, null, false);
    }

    private void setupActions() {
        logoutButton.addActionListener(e -> Controller.showLogin());

        createBachecaButton.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
            NuovaBachecaDialog dialog = new NuovaBachecaDialog(parentFrame, () -> aggiornaListaBacheche(null, null, false));
            dialog.setVisible(true);
        });

        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();
            String dateText = dateField.getText().trim();
            Date filterDate = null;
            if (!dateText.isEmpty()) {
                try {
                    filterDate = new SimpleDateFormat("dd/MM/yyyy").parse(dateText);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Formato data non valido. Usa dd/MM/yyyy", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            aggiornaListaBacheche(query.isEmpty() ? null : query, filterDate, false);
        });

        todayButton.addActionListener(e -> aggiornaListaBacheche(null, new Date(), true));
    }

    private void aggiornaListaBacheche(String searchQuery, Date filterDate, boolean onlyToday) {
        bachechePanel.removeAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Collection<Bacheca> bacheche = Controller.getBacheche().values();
        Date today = new Date();

        if (bacheche.isEmpty()) {
            JLabel noBacheche = new JLabel("Nessuna bacheca disponibile.");
            noBacheche.setFont(new Font("Arial", Font.BOLD, 16));
            noBacheche.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.add(noBacheche, BorderLayout.CENTER);
            bachechePanel.add(wrap);
        }

        for (Bacheca b : bacheche) {
            JPanel bachecaBox = new JPanel();
            bachecaBox.setLayout(new BoxLayout(bachecaBox, BoxLayout.Y_AXIS));
            bachecaBox.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    b.getTitolo().name() + " - " + b.getDescrizione(),
                    TitledBorder.LEFT, TitledBorder.TOP));

            JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton editBachecaButton = new JButton("Modifica");
            editBachecaButton.addActionListener(e -> Controller.showEditBachecaDialog(b.getTitolo()));
            header.add(editBachecaButton);

            JButton deleteBachecaButton = new JButton("Elimina");
            deleteBachecaButton.addActionListener(e -> Controller.showDeleteBachecaConfirmation(b.getTitolo()));
            header.add(deleteBachecaButton);
            bachecaBox.add(header);

            List<ToDo> todos = b.getToDoList();

            if (searchQuery != null) {
                todos = todos.stream()
                        .filter(todo -> todo.getTitolo().toLowerCase().contains(searchQuery))
                        .collect(Collectors.toList());
            }

            if (filterDate != null) {
                String formattedFilterDate = sdf.format(filterDate);
                todos = todos.stream()
                        .filter(todo -> sdf.format(todo.getScadenza()).equals(formattedFilterDate))
                        .collect(Collectors.toList());
            }

            if (todos.isEmpty()) {
                JLabel noTodosLabel = new JLabel("ToDo non sono presenti.");
                noTodosLabel.setForeground(Color.DARK_GRAY);
                bachecaBox.add(noTodosLabel);
            } else {
                todos.sort(Comparator.comparingInt(ToDo::getPosizione));
                for (ToDo todo : todos) {
                    JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    Color sfondo = Color.WHITE;
                    String coloreStr = todo.getColoreSfondo();
                    if (coloreStr != null && !coloreStr.isEmpty()) {
                        try {
                            sfondo = Color.decode(coloreStr);
                        } catch (NumberFormatException ex) {
                            System.err.println("Colore non valido: " + coloreStr);
                        }
                    }
                    row.setBackground(sfondo);

                    JCheckBox checkBox = new JCheckBox(todo.getTitolo() + " - Scadenza: " + sdf.format(todo.getScadenza()));
                    checkBox.setSelected(todo.isCompletato());
                    if (todo.getScadenza().before(today)) checkBox.setForeground(Color.RED);
                    checkBox.addActionListener(e -> todo.setCompletato(checkBox.isSelected()));
                    row.add(checkBox);

                    if (todo.getImmaginePath() != null && !todo.getImmaginePath().isEmpty()) {
                        try {
                            ImageIcon icon = new ImageIcon(todo.getImmaginePath());
                            Image image = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            JLabel imageLabel = new JLabel(new ImageIcon(image));
                            imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            imageLabel.setToolTipText("Clicca per ingrandire");
                            imageLabel.addMouseListener(new MouseAdapter() {
                                public void mouseClicked(MouseEvent e) {
                                    showImagePopup(todo.getImmaginePath());
                                }
                            });
                            row.add(imageLabel);
                        } catch (Exception ex) {
                            JLabel errorLabel = new JLabel("Immagine non valida");
                            errorLabel.setForeground(Color.RED);
                            row.add(errorLabel);
                        }
                    }

                    JButton viewToDoButton = new JButton("Visualizza");
                    viewToDoButton.addActionListener(e -> {
                        StringBuilder info = new StringBuilder();
                        info.append("<html><body style='width: 380px;'>");
                        info.append("<b>Titolo:</b> ").append(todo.getTitolo()).append("<br>");
                        info.append("<b>Descrizione:</b> ").append(todo.getDescrizione()).append("<br>");
                        info.append("<b>Scadenza:</b> ").append(sdf.format(todo.getScadenza())).append("<br>");
                        info.append("<b>Completato:</b> ").append(todo.isCompletato() ? "S\u00EC" : "No").append("<br>");
                        info.append("<b>Autore:</b> ").append(todo.getAutore() != null ? todo.getAutore() : "N/A").append("<br>");

                        if (todo.getUrl() != null && !todo.getUrl().isEmpty()) {
                            info.append("<b>URL:</b> <a href='").append(todo.getUrl()).append("'>").append(todo.getUrl()).append("</a><br>");
                        }

                        if (todo.getColoreSfondo() != null && !todo.getColoreSfondo().isEmpty()) {
                            info.append("<b>Colore Sfondo:</b> ").append(todo.getColoreSfondo()).append("<br>");
                        }

                        if (todo.getImmaginePath() != null && !todo.getImmaginePath().isEmpty()) {
                            info.append("<b>Immagine:</b> ").append(todo.getImmaginePath()).append("<br>");
                        }

                        info.append("</body></html>");

                        JEditorPane editorPane = new JEditorPane("text/html", info.toString());
                        editorPane.setEditable(false);
                        editorPane.setOpaque(false);
                        editorPane.addHyperlinkListener(event -> {
                            if (event.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                                try {
                                    Desktop.getDesktop().browse(event.getURL().toURI());
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(mainPanel, "Errore nell'apertura del link", "Errore", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        });

                        JScrollPane scrollPane = new JScrollPane(editorPane);
                        scrollPane.setPreferredSize(new Dimension(400, 300));

                        JOptionPane.showMessageDialog(mainPanel, scrollPane, "Dettagli ToDo", JOptionPane.INFORMATION_MESSAGE);
                    });
                    row.add(viewToDoButton);

                    JButton mostraUtentiBtn = new JButton("Mostra utenti condivisi");
                    mostraUtentiBtn.addActionListener(e -> {
                        Controller.gestisciUtentiCondivisi(todo, mainPanel);
                    });
                    row.add(mostraUtentiBtn);


                    JButton editToDoButton = new JButton("Modifica");
                    editToDoButton.addActionListener(e -> Controller.showEditToDoWindow(b.getTitolo(), todo));
                    row.add(editToDoButton);

                    JButton deleteToDoButton = new JButton("Elimina");
                    deleteToDoButton.addActionListener(e -> {
                        if (JOptionPane.showConfirmDialog(mainPanel, "Eliminare il ToDo '" + todo.getTitolo() + "'?", "Conferma", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            Controller.eliminaToDo(b.getTitolo(), todo);
                            aggiornaListaBacheche(searchQuery, filterDate, onlyToday);
                        }
                    });
                    row.add(deleteToDoButton);

                    JButton upBtn = new JButton("↑");
                    upBtn.addActionListener(e -> {
                        Controller.spostaToDo(b.getTitolo(), todo, -1);
                        aggiornaListaBacheche(searchQuery, filterDate, onlyToday);
                    });
                    row.add(upBtn);

                    JButton downBtn = new JButton("↓");
                    downBtn.addActionListener(e -> {
                        Controller.spostaToDo(b.getTitolo(), todo, 1);
                        aggiornaListaBacheche(searchQuery, filterDate, onlyToday);
                    });
                    row.add(downBtn);

                    JButton moveBtn = new JButton("Sposta");
                    moveBtn.addActionListener(e -> {
                        TitoloBacheca nuovaBacheca = (TitoloBacheca) JOptionPane.showInputDialog(
                                mainPanel,
                                "Sposta ToDo in quale bacheca?",
                                "Sposta ToDo",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                Controller.getBacheche().keySet().toArray(),
                                b.getTitolo());
                        if (nuovaBacheca != null && nuovaBacheca != b.getTitolo()) {
                            Controller.spostaToDoInAltraBacheca(b.getTitolo(), nuovaBacheca, todo);
                            aggiornaListaBacheche(searchQuery, filterDate, onlyToday);
                        }
                    });
                    row.add(moveBtn);
                    JButton condividiBtn = new JButton("Condividi");
                    condividiBtn.setEnabled(todo.isUtenteAutore(Controller.getUtenteLoggato()));
                    condividiBtn.addActionListener(e -> {
                        List<String> utentiDisponibili = Controller.getTuttiUtenti().stream()
                                .filter(u -> !u.equals(todo.getAutore()))
                                .toList();

                        if (utentiDisponibili.isEmpty()) {
                            JOptionPane.showMessageDialog(mainPanel, "Nessun utente disponibile per la condivisione.", "Info", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }

                        JList<String> lista = new JList<>(utentiDisponibili.toArray(new String[0]));
                        lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                        JScrollPane scrollPane = new JScrollPane(lista);
                        scrollPane.setPreferredSize(new Dimension(300, 150));

                        int result = JOptionPane.showConfirmDialog(mainPanel, scrollPane, "Condividi con utenti", JOptionPane.OK_CANCEL_OPTION);
                        if (result == JOptionPane.OK_OPTION) {
                            List<String> utentiSelezionati = lista.getSelectedValuesList();
                            if (!utentiSelezionati.isEmpty()) {
                                for (String utente : utentiSelezionati) {
                                    if (!todo.getListaUtenti().contains(utente)) {
                                        Controller.inviaRichiestaCondivisione(todo, utente);  // ✅ QUI LO USI
                                    }
                                }
                            }
                        }
                    });
                    row.add(condividiBtn);


                    bachecaBox.add(row);
                }

            }

            JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton addToDoButton = new JButton("Aggiungi ToDo");
            addToDoButton.addActionListener(e -> Controller.showToDoWindow(b.getTitolo()));
            addPanel.add(addToDoButton);
            bachecaBox.add(addPanel);
            bachechePanel.add(bachecaBox);
        }

        bachechePanel.revalidate();
        bachechePanel.repaint();
    }

    private void showImagePopup(String imagePath) {
        JFrame frame = new JFrame("Immagine del ToDo");
        ImageIcon icon = new ImageIcon(imagePath);
        JLabel label = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH)));
        frame.add(label);
        frame.setSize(420, 440);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    private void setupUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createBachecaButton = new JButton("Crea Bacheca");
        logoutButton = new JButton("Logout");
        searchField = new JTextField(15);
        dateField = new JTextField(10);
        searchButton = new JButton("Cerca");
        todayButton = new JButton("Scadenze Oggi");

        topPanel.add(createBachecaButton);
        topPanel.add(logoutButton);
        topPanel.add(new JLabel("Titolo:"));
        topPanel.add(searchField);
        topPanel.add(new JLabel("Data (dd/MM/yyyy):"));
        topPanel.add(dateField);
        topPanel.add(searchButton);
        topPanel.add(todayButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        bachechePanel = new JPanel();
        bachechePanel.setLayout(new BoxLayout(bachechePanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(bachechePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }
}
