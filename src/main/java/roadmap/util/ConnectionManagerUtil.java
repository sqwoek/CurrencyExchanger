package roadmap.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import roadmap.exception.DatabaseException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManagerUtil {
    private static final String PROPERTIES_PATH = "application.properties";
    private static final HikariDataSource dataSource;

    private ConnectionManagerUtil() {
    }

    static {
        try {
            Properties properties = new Properties();
            InputStream input = ConnectionManagerUtil.class
                    .getClassLoader()
                    .getResourceAsStream(PROPERTIES_PATH);
            if (input != null) {
                properties.load(input);
            }

            String url = properties.getProperty(
                    "database.url",
                    "jdbc:sqlite:D:/idea_projects/CurrencyExchanger/currency_db.sqlite"
            );
            String driverName = properties.getProperty("driver.className", "org.sqlite.JDBC");
            Class.forName(driverName);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);

            dataSource = new HikariDataSource(config);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Failed to load SQLite drive" + ex);
        } catch (Exception ex) {
            throw new DatabaseException("Failed to connect to DB." + ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}