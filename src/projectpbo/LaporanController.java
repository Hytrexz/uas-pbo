package projectpbo;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import java.sql.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.*;
import javafx.scene.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.*;
import java.io.File;
import java.time.LocalDate;


public class LaporanController {
    @FXML private Button btnPemasukan, btnPengeluaran, btnTabungan;
    @FXML private TableView<ObservableList<String>> table;
    @FXML private TableColumn<ObservableList<String>, String> colNo, colJumlah, colKet, colWaktu;
    @FXML private Label lblTotalPengeluaran, lblTotalPemasukan, lblTotalTabungan, lblSaldo, lblError;
    private String tipeAktif;
    
    @FXML
    private void initialize() {
        colNo.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(table.getItems().indexOf(d.getValue()) + 1)));
        colJumlah.setCellValueFactory(d -> new SimpleStringProperty("Rp" + d.getValue().get(0)));
        colKet.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colWaktu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));

        loadAll();
    }

    private void loadAll() {
        loadTotalPemasukan();
        loadTotalPengeluaran();
        loadTotalTabungan();
        loadSaldo();
    }
    
    private void loadTotalPemasukan() {
        try (Connection c = Database.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT COALESCE(SUM(jumlah),0) FROM transaksi WHERE saldo_id=? AND tipe='PEMASUKAN'")) {
            
            ps.setInt(1, Session.accountId);
            ResultSet r = ps.executeQuery();
            
            if (r.next())
                lblTotalPemasukan.setText("Total Pemasukan : Rp" + r.getDouble(1));
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    private void loadTotalPengeluaran() {
        try (Connection c = Database.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT COALESCE(SUM(jumlah),0) FROM transaksi WHERE saldo_id=? AND tipe='PENGELUARAN'")) {
            
            ps.setInt(1, Session.accountId);
            ResultSet r = ps.executeQuery();
            
            if (r.next())
                lblTotalPengeluaran.setText("Total Pengeluaran : Rp" + r.getDouble(1));
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    private void loadTotalTabungan() {
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
    private void pemasukan() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        tipeAktif = "PEMASUKAN";
        lblError.setText("");

        try (Connection c = Database.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT id,jumlah,ket,waktu FROM transaksi WHERE saldo_id=? AND tipe='PEMASUKAN' ORDER BY waktu DESC")) {
            
            ps.setInt(1, Session.accountId);
            ResultSet r = ps.executeQuery();
            
            btnPemasukan.setDisable(true);
            btnPengeluaran.setDisable(false);
            btnTabungan.setDisable(false);

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

    @FXML
    private void pengeluaran() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        tipeAktif = "PENGELUARAN";
        lblError.setText("");

        try (Connection c = Database.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT id,jumlah,ket,waktu FROM transaksi WHERE saldo_id=? AND tipe='PENGELUARAN' ORDER BY waktu DESC")) {
            
            ps.setInt(1, Session.accountId);
            ResultSet r = ps.executeQuery();

            btnPemasukan.setDisable(false);
            btnPengeluaran.setDisable(true);
            btnTabungan.setDisable(false);            

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
    
    @FXML
    private void tabungan() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        tipeAktif = "TABUNGAN";
        lblError.setText("");

        try (Connection c = Database.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT id,jumlah,ket,waktu FROM transaksi WHERE saldo_id=? AND tipe='TABUNGAN' ORDER BY waktu DESC")) {
            
            ps.setInt(1, Session.accountId);
            ResultSet r = ps.executeQuery();

            btnPemasukan.setDisable(false);
            btnPengeluaran.setDisable(false);
            btnTabungan.setDisable(true);            

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
    
    @FXML
    private void downloadPDF() {
        if (table.getItems().isEmpty()) {
            lblError.setText("Tidak Ada Data!");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF File", "*.pdf"));

        File file = fc.showSaveDialog(table.getScene().getWindow());
        if (file == null) return;

        buatPDF(file);
    }    
    
    private void buatPDF(File file) {
        String nama = "";
        try (Connection c = Database.getConnection();PreparedStatement ps = c.prepareStatement("SELECT username FROM users WHERE id=?")) {
            ps.setInt(1, Session.accountId);
            ResultSet r = ps.executeQuery();
            
            if (r.next()) {
                nama = r.getString("username");
            }
            
        } catch (Exception e) {
            System.out.println(e);
        }        
          
        try {
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            
            doc.add(new Paragraph("LAPORAN " + tipeAktif).setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Username : " + nama));
            doc.add(new Paragraph("Dicetak : " + LocalDate.now()));
            doc.add(new Paragraph("\n"));

            Table pdfTable = new Table(4);
            pdfTable.addHeaderCell("No");
            pdfTable.addHeaderCell("Jumlah");
            pdfTable.addHeaderCell("Keterangan");
            pdfTable.addHeaderCell("Waktu");

            int no = 1;
            for (ObservableList<String> row : table.getItems()) {
                pdfTable.addCell(String.valueOf(no++));
                pdfTable.addCell("Rp " + row.get(0));
                pdfTable.addCell(row.get(1));
                pdfTable.addCell(row.get(2));
            }

            doc.add(pdfTable);

            doc.close();

        } catch (Exception e) {
            System.out.println(e);
        }
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
}
