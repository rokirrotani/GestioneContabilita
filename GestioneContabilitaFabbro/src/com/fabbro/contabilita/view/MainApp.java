package com.fabbro.contabilita.view;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.fabbro.contabilita.database.DatabaseConnection;
import com.fabbro.contabilita.model.Transazione;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gestione Contabilità Fabbro");

        // Layout principale
        BorderPane rootLayout = new BorderPane();

        // VBox per i campi di input
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        // Campo tipo (Entrata o Uscita)
        HBox tipoBox = new HBox();
        Label tipoLabel = new Label("Tipo (Entrata/Uscita): ");
        TextField tipoField = new TextField();
        tipoBox.getChildren().addAll(tipoLabel, tipoField);
        tipoBox.setSpacing(10);

        // Campo importo
        HBox importoBox = new HBox();
        Label importoLabel = new Label("Importo: ");
        TextField importoField = new TextField();
        importoBox.getChildren().addAll(importoLabel, importoField);
        importoBox.setSpacing(10);

        // Campo descrizione
        HBox descrizioneBox = new HBox();
        Label descrizioneLabel = new Label("Descrizione: ");
        TextField descrizioneField = new TextField();
        descrizioneBox.getChildren().addAll(descrizioneLabel, descrizioneField);
        descrizioneBox.setSpacing(10);

        // Campo data
        HBox dataBox = new HBox();
        Label dataLabel = new Label("Data (AAAA-MM-GG): ");
        TextField dataField = new TextField();
        dataBox.getChildren().addAll(dataLabel, dataField);
        dataBox.setSpacing(10);

        // Pulsanti
        HBox buttonBox = new HBox();
        Button saveButton = new Button("Salva");
        Button resetButton = new Button("Reset");
        buttonBox.getChildren().addAll(saveButton, resetButton);
        buttonBox.setSpacing(10);

        // Aggiunta dei campi e pulsanti alla VBox
        vbox.getChildren().addAll(tipoBox, importoBox, descrizioneBox, dataBox, buttonBox);

        // Tabella per visualizzare le transazioni
        TableView<Transazione> transazioniTable = new TableView<>();
        TableColumn<Transazione, String> tipoColumn = new TableColumn<>("Tipo");
        tipoColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        TableColumn<Transazione, Double> importoColumn = new TableColumn<>("Importo");
        importoColumn.setCellValueFactory(new PropertyValueFactory<>("importo"));

        TableColumn<Transazione, String> descrizioneColumn = new TableColumn<>("Descrizione");
        descrizioneColumn.setCellValueFactory(new PropertyValueFactory<>("descrizione"));

        TableColumn<Transazione, String> dataColumn = new TableColumn<>("Data");
        dataColumn.setCellValueFactory(new PropertyValueFactory<>("data"));

        transazioniTable.getColumns().addAll(tipoColumn, importoColumn, descrizioneColumn, dataColumn);
        transazioniTable.setItems(getTransazioniList());

        vbox.getChildren().add(transazioniTable);

        // Grafico a torta per le entrate e uscite
        PieChart pieChart = new PieChart();
        int totaleEntrate = getTotaleTransazione("Entrata");
        int totaleUscite = getTotaleTransazione("Uscita");

        PieChart.Data slice1 = new PieChart.Data("Entrate", totaleEntrate);
        PieChart.Data slice2 = new PieChart.Data("Uscite", totaleUscite);

        pieChart.getData().add(slice1);
        pieChart.getData().add(slice2);

        vbox.getChildren().add(pieChart);

        // Posiziona VBox al centro del BorderPane
        rootLayout.setCenter(vbox);

        // Scena principale
        Scene scene = new Scene(rootLayout, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Crea la tabella delle transazioni se non esiste
        DatabaseConnection.createTransazioniTable();

        // Azione del pulsante "Salva"
        saveButton.setOnAction(e -> {
            String tipo = tipoField.getText();
            String importoText = importoField.getText();
            String descrizione = descrizioneField.getText();
            String data = dataField.getText();

            try {
                double importo = Double.parseDouble(importoText);
                saveTransazione(tipo, importo, descrizione, data);
                System.out.println("Transazione salvata con successo.");

                // Resetta i campi dopo il salvataggio
                tipoField.clear();
                importoField.clear();
                descrizioneField.clear();
                dataField.clear();

                // Aggiorna la tabella e il grafico
                transazioniTable.setItems(getTransazioniList());
                pieChart.getData().clear();
                pieChart.getData().add(new PieChart.Data("Entrate", getTotaleTransazione("Entrata")));
                pieChart.getData().add(new PieChart.Data("Uscite", getTotaleTransazione("Uscita")));
            } catch (NumberFormatException ex) {
                System.out.println("Errore: importo non valido.");
            }
        });

        // Azione del pulsante "Reset"
        resetButton.setOnAction(e -> {
            tipoField.clear();
            importoField.clear();
            descrizioneField.clear();
            dataField.clear();
        });
    }

    private void saveTransazione(String tipo, double importo, String descrizione, String data) {
        String sql = "INSERT INTO transazioni(tipo, importo, descrizione, data) VALUES(?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(sql) : null) {
            
            if (conn == null || pstmt == null) {
                System.out.println("Errore: connessione al database fallita. Impossibile salvare la transazione.");
                return;
            }

            pstmt.setString(1, tipo);
            pstmt.setDouble(2, importo);
            pstmt.setString(3, descrizione);
            pstmt.setString(4, data);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Errore durante l'inserimento della transazione: " + e.getMessage());
        }
    }

    private ObservableList<Transazione> getTransazioniList() {
        ObservableList<Transazione> transazioniList = FXCollections.observableArrayList();

        String sql = "SELECT * FROM transazioni";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn != null ? conn.createStatement() : null;
             ResultSet rs = stmt != null ? stmt.executeQuery(sql) : null) {

            if (rs == null) {
                System.out.println("Errore: nessun risultato ottenuto.");
                return transazioniList;
            }

            while (rs.next()) {
                Transazione transazione = new Transazione(
                        rs.getInt("id"),
                        rs.getString("tipo"),
                        rs.getDouble("importo"),
                        rs.getString("descrizione"),
                        rs.getString("data")
                );
                transazioniList.add(transazione);
            }
        } catch (SQLException e) {
            System.out.println("Errore durante il recupero delle transazioni: " + e.getMessage());
        }

        return transazioniList;
    }

    private int getTotaleTransazione(String tipo) {
        String sql = "SELECT SUM(importo) AS totale FROM transazioni WHERE tipo = ?";
        int totale = 0;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tipo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totale = rs.getInt("totale");
            }
        } catch (SQLException e) {
            System.out.println("Errore durante il calcolo del totale delle transazioni: " + e.getMessage());
        }

        return totale;
    }

    public static void main(String[] args) {
        launch(args);
    }
}