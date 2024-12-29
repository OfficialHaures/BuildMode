package nl.inferno.buildMode.database;

import nl.inferno.buildMode.main.BuildMode;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final BuildMode plugin;
    private Connection connection;

    public DatabaseManager(BuildMode plugin) {
        this.plugin = plugin;
        createDefaultConfig();
        connect();
        createTables();
    }

    private void createDefaultConfig() {
        FileConfiguration config = plugin.getConfig();
        config.addDefault("database.host", "localhost");
        config.addDefault("database.port", 3306);
        config.addDefault("database.name", "buildmode");
        config.addDefault("database.username", "root");
        config.addDefault("database.password", "");
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    private void connect() {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.name");
        String username = config.getString("database.username");
        String password = config.getString("database.password");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=false&autoReconnect=true",
                username,
                password
            );
            plugin.getLogger().info("Database connection established!");
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Could not connect to database! Error: " + e.getMessage());
        }
    }

    private void createTables() {
        try {
            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS build_mode_inventories (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "inventory TEXT," +
                "armor TEXT," +
                "active BOOLEAN DEFAULT FALSE," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
            );
            plugin.getLogger().info("Database tables created successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create tables! Error: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Connection check failed! Error: " + e.getMessage());
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not close database connection! Error: " + e.getMessage());
        }
    }
}
