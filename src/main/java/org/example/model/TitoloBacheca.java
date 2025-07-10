package org.example.model;

public enum TitoloBacheca {
    UNIVERSITA("Università"),
    LAVORO("Lavoro"),
    TEMPO_LIBERO("Tempo Libero");

    private final String label;

    TitoloBacheca(String label) {
        this.label = label;
    }


    @Override
    public String toString() {
        return label;
    }


    public String getKey() {
        return name();
    }
}
