package projectpbo;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TabunganController {
    @FXML private TextField jumlahField, ketField;
    @FXML private Button btnAmbil, btnEdit;
    @FXML private TableView<ObservableList<String>> table;
    @FXML private TableColumn<ObservableList<String>, String> colNo, colJumlah, colKet, colWaktu;
    @FXML private Label lblTotalTabungan, lblSaldo, lblError;

    private int selectedId = -1;
    private double selectedJumlah = 0;

    @FXML
    private void initialize() {
        colNo.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(table.getItems().indexOf(d.getValue()) + 1)));
        colJumlah.setCellValueFactory(d -> new SimpleStringProperty("Rp" + d.getValue().get(0)));
        colKet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colWaktu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));

        btnEdit.setDisable(true);
        btnAmbil.setDisable(true);

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedId = Integer.parseInt(n.get(3));
                selectedJumlah = Double.parseDouble(n.get(0));
                jumlahField.setText(n.get(0));
                ketField.setText(n.get(1));
                btnEdit.setDisable(false);                
                btnAmbil.setDisable(false);
            } else {
                reset();
            }
        });
        
        loadAll();
    }

    private void loadAll() {
        loadTable();
        loadTotal();
        loadSaldo();
    }

    private void loadTable() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id,jumlah,ket,waktu FROM transaksi WHERE saldo_id=? AND tipe='TABUNGAN' ORDER BY waktu DESC")) {

            ps.setInt(1, Session.accountId);
            ResultSet r = ps.executeQuery();

            while (r.next()) {
                data.add(FXCollections.observableArrayList(
                    r.getString("jumlah"),
                    r.getString("ket"),
                    r.getString("waktu"),
                    String.valueOf(r.getInt("id"))
                ));
            }
            table.setItems(data);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void loadTotal() {
        try (Connection c = Database.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT COALESCE(SUM(jumlah),0) FROM transaksi WHERE saldo_id=? AND tipe='TABUNGAN'")) {

            ps.setInt(1, Session.accountId);
            ResultSet r = ps.executeQuery();
            
            if (r.next())
                lblTotalTabungan.setText("Total Tabungan : Rp" + r.getDouble(1));
        } catch (Exception e) {}
    }

    private void loadSaldo() {
        try (Connection c = Database.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT saldo FROM saldo WHERE id=?")) {

            ps.setInt(1, Session.accountId);
            ResultSet r = ps.executeQuery();
            
            if (r.next())
                lblSaldo.setText("Saldo : Rp" + r.getDouble(1));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @FXML
    private void tabung() throws SQLException {
        lblError.setText("");
        try {
            double j = Double.parseDouble(jumlahField.getText());
            
            if (j <= 0) {
                lblError.setText("Jumlah Tidak Boleh Negatif!");
                return;
            }         
            
            try (Connection c = Database.getConnection()) {PreparedStatement psSaldo = c.prepareStatement("SELECT saldo FROM saldo WHERE id=?");
                psSaldo.setInt(1, Session.accountId);
                ResultSet rs = psSaldo.executeQuery();
                rs.next();
                
                double saldo = rs.getDouble(1);

                if (saldo < j) {
                    lblError.setText("Saldo Tidak Cukup!");
                    return;
                }

                c.setAutoCommit(false);
                c.prepareStatement("INSERT INTO transaksi (saldo_id,tipe,jumlah,ket) VALUES (" + Session.accountId + ",'TABUNGAN'," + 
                        j + ",'" + ketField.getText() + "')").executeUpdate();
                c.prepareStatement("UPDATE saldo SET saldo = saldo - " + j + " WHERE id=" + Session.accountId).executeUpdate();
                c.commit();
            }
        reset();
        loadAll();

        } catch (NumberFormatException e) {
            lblError.setText("Jumlah Harus Berupa Angka!");
        }
    }

    @FXML
    private void edit() {
        if (selectedId == -1) return;
        
        try {
            double newJumlah = Double.parseDouble(jumlahField.getText());
            double selisih = newJumlah - selectedJumlah;
            
            if (newJumlah <= 0) {
                lblError.setText("Jumlah Tidak Boleh Negatif!");
                return;
            }            
            
            try (Connection c = Database.getConnection()) {
                c.setAutoCommit(false);
                c.prepareStatement("UPDATE transaksi SET jumlah=" + newJumlah + ", ket='" + ketField.getText() + "' WHERE id=" + selectedId).executeUpdate();
                c.prepareStatement("UPDATE saldo SET saldo = saldo - " + selisih + " WHERE id=" + Session.accountId).executeUpdate();
                c.commit();
                
            } catch (NumberFormatException e) {
                lblError.setText("Jumlah Harus Berupa Angka!");
            }
            
        } catch (Exception e) {
            System.out.println(e);
        }
        reset();
        loadAll();
    }     
    
    @FXML
    private void ambil() {
        if (selectedId == -1) return;

        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            c.prepareStatement("DELETE FROM transaksi WHERE id=" + selectedId).executeUpdate();
            c.prepareStatement("UPDATE saldo SET saldo = saldo + " + selectedJumlah + " WHERE id=" + Session.accountId).executeUpdate();
            c.commit();
        } catch (Exception e) {
            System.out.println(e);
        }
        reset();
        loadAll();
    }
    
    @FXML
    private void back() {
        try{
            Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
            Stage stage = (Stage) table.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    private void reset() {
        selectedId = -1;
        selectedJumlah = 0;
        jumlahField.clear();
        ketField.clear();
        btnEdit.setDisable(true);
        btnAmbil.setDisable(true);
        table.getSelectionModel().clearSelection();
    }
}
