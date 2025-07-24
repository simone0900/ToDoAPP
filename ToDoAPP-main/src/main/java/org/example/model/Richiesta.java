package org.example.model;

import java.sql.Timestamp;

public class Richiesta {
    private int id;
    private int idToDo;
    private String mittente;
    private String destinatario;
    private String titoloBacheca;
    private String stato; // "in_attesa", "accettata", "rifiutata"
    private Timestamp dataRichiesta;

    // Costruttore base
    public Richiesta(int idToDo, String mittente, String destinatario, String titoloBacheca) {
        this.idToDo = idToDo;
        this.mittente = mittente;
        this.destinatario = destinatario;
        this.titoloBacheca = titoloBacheca;
        this.stato = "in_attesa";
        this.dataRichiesta = new Timestamp(System.currentTimeMillis());
    }

    // Costruttore completo (utile per il DAO)
    public Richiesta(int id, int idToDo, String mittente, String destinatario, String titoloBacheca, String stato, Timestamp dataRichiesta) {
        this.id = id;
        this.idToDo = idToDo;
        this.mittente = mittente;
        this.destinatario = destinatario;
        this.titoloBacheca = titoloBacheca;
        this.stato = stato;
        this.dataRichiesta = dataRichiesta;
    }

    // Getter e Setter
    public int getId() {
        return id;
    }

    public int getIdToDo() {
        return idToDo;
    }


    public String getMittente() {
        return mittente;
    }


    public String getDestinatario() {
        return destinatario;
    }


    public String getTitoloBacheca() {
        return titoloBacheca;
    }




    @Override
    public String toString() {
        return "Richiesta{" +
                "id=" + id +
                ", idToDo=" + idToDo +
                ", mittente='" + mittente + '\'' +
                ", destinatario='" + destinatario + '\'' +
                ", titoloBacheca='" + titoloBacheca + '\'' +
                ", stato='" + stato + '\'' +
                ", dataRichiesta=" + dataRichiesta +
                '}';
    }
}
