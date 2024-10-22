package com.fabbro.contabilita.model;

public class Transazione {
    private int id;
    private String tipo;
    private double importo;
    private String descrizione;
    private String data;

    public Transazione(int id, String tipo, double importo, String descrizione, String data) {
        this.id = id;
        this.tipo = tipo;
        this.importo = importo;
        this.descrizione = descrizione;
        this.data = data;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getImporto() {
        return importo;
    }

    public void setImporto(double importo) {
        this.importo = importo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}