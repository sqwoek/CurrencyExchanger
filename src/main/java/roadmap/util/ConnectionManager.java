package roadmap.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String DB_PATH = "D:/demo/CurrencyExchanger/currency_db.sqlite";
    private static final HikariDataSource dataSource;

    private ConnectionManager() {
    }

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DB_PATH;

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);

            dataSource = new HikariDataSource(config);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("SQLite drive is not found" + ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}