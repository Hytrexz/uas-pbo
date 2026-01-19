package projectpbo;

import java.sql.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MenuController{
    
    @FXML private Label lblSaldo;
    @FXML private AnchorPane rootPane;

    @FXML
    private void initialize() {
        loadSaldo();
    }

    @FXML 
    private void pemasukan() {
        load("pemasukan.fxml");
    }

    @FXML
    private void pengeluaran() {
        load("pengeluaran.fxml");
    }

    @FXML
    private void tabungan() {
        load("tabungan.fxml");
    }

    @FXML
    private void laporan() {
        load("laporan.fxml");
    }

    private void load(String fxml) {
    try {
        Parent halaman = FXMLLoader.load(getClass().getResource(fxml));
        
        rootPane.getChildren().clear();
        rootPane.getChildren().add(halaman);
        
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    private void loadSaldo() {
        try (Connection c = Database.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT saldo FROM saldo WHERE id=?")) {

            ps.setInt(1, Session.accountId);
            ResultSet r = ps.executeQuery();

            if (r.next()) {
                lblSaldo.setText("Saldo : Rp " + r.getDouble(1));
            }

        } catch (Exception e) {
            System.out.println(e);  
        }
    }
    
    @FXML
    private void keluar() {
        try {
            Session.accountId = 0;

            Parent login = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene loginScene = new Scene(login);

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            System.out.println(e);            
        }  
    }
}