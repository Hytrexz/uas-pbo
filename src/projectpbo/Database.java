package projectpbo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    protected static String URL = "jdbc:postgresql://127.0.0.1:5432/projectpbo";
    protected static String USER = "postgres";
    protected static String PASS = "passsqlkiki12";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Koneksi database gagal!");
            return null;
        }
    }
}
