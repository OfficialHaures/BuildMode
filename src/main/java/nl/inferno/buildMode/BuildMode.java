package nl.inferno.buildMode;

import nl.inferno.buildMode.database.DatabaseManager;
import nl.inferno.buildMode.listeners.BuildModeListener;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class BuildMode extends JavaPlugin {
    private static BuildMode instance;
    private DatabaseManager databaseManager;
    private final Map<UUID, BuildModeData> buildModePlayers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        databaseManager = new DatabaseManager(this);
        getCommand("build").setExecutor(new BuildCommand(this));
        getServer().getPluginManager().registerEvents(new BuildModeListener(this), this);
        getLogger().info("BuildMode has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("BuildMode has been disabled!");
    }

    public boolean isInBuildMode(Player player) {
        return buildModePlayers.containsKey(player.getUniqueId());
    }

    public void enableBuildMode(Player player) {
        BuildModeData data = new BuildModeData(player);
        buildModePlayers.put(player.getUniqueId(), data);

        player.getInventory().clear();
        player.setGameMode(GameMode.CREATIVE);

        for (Player online : getServer().getOnlinePlayers()) {
            if (!online.hasPermission("buildmode.see")) {
                online.hidePlayer(this, player);
            }
        }

        player.sendMessage("§aYou are now in build mode!");
    }

    public void disableBuildMode(Player player) {
        BuildModeData data = buildModePlayers.remove(player.getUniqueId());

        if (data != null) {
            data.restoreInventory(player);
            player.setGameMode(GameMode.SURVIVAL);

            for (Player online : getServer().getOnlinePlayers()) {
                online.showPlayer(this, player);
            }

            player.sendMessage("§aYou are no longer in build mode!");
        }
    }

    public static BuildMode getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public boolean wasInBuildMode(UUID uuid) {
        try {
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(
                "SELECT * FROM build_mode_inventories WHERE uuid = ?"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Set<Player> getBuildModePlayers() {
        Set<Player> players = new HashSet<>();
        buildModePlayers.keySet().forEach(uuid -> {
            Player player = getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        });
        return players;
    }

    public void saveBuildModeState(Player player) {
        if (isInBuildMode(player)) {
            try {
                PreparedStatement ps = databaseManager.getConnection().prepareStatement(
                    "UPDATE build_mode_inventories SET active = true WHERE uuid = ?"
                );
                ps.setString(1, player.getUniqueId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
