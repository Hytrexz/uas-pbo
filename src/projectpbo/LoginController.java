package projectpbo;

import java.sql.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    
    @FXML
    private void login() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        String sql = "SELECT * FROM users WHERE username=? AND password=?";

        try (Connection conn = Database.getConnection();PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Session.accountId = rs.getInt("id");
                menu();
            } else {
                error("Login Gagal! Username atau password salah");
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void menu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menu.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtUsername.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Sistem Manajemen Keuangan");
            stage.show();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void error(String msg) {
        lblError.setText(msg);
    }
}
