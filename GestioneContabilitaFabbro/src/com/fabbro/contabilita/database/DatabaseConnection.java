package com.fabbro.contabilita.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Percorso del database SQLite
    private static final String URL = "jdbc:sqlite:contabilita.db"; // Puoi sostituire questo con un percorso assoluto

    // Metodo per connettersi al database
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            if (conn != null) {
                System.out.println("Connessione al database stabilita.");
            }
        } catch (SQLException e) {
            System.out.println("Errore di connessione al database: " + e.getMessage());
        }
        return conn;
    }

    // Metodo per creare la tabella delle transazioni se non esiste
    public static void createTransazioniTable() {
        String sql = "CREATE TABLE IF NOT EXISTS transazioni (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "tipo TEXT NOT NULL, " +
                     "importo REAL NOT NULL, " +
                     "descrizione TEXT, " +
                     "data TEXT NOT NULL" +
                     ");";

        try (Connection conn = connect(); Statement stmt = conn != null ? conn.createStatement() : null) {
            if (stmt != null) {
                stmt.execute(sql);
                System.out.println("Tabella 'transazioni' creata o già esistente.");
            } else {
                System.out.println("Connessione al database fallita, impossibile creare la tabella.");
            }
        } catch (SQLException e) {
            System.out.println("Errore durante la creazione della tabella: " + e.getMessage());
        }
    }
}