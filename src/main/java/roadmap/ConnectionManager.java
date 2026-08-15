package roadmap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String DB_PATH = "D:/demo/CurrencyExchanger/currency_db.sqlite";

    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("SQLite driver is not found" + ex);
        }

        String url = "jdbc:sqlite:" + DB_PATH;
        return DriverManager.getConnection(url);
    }
}