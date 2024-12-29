package nl.inferno.buildMode.database;

import com.google.gson.Gson;
import nl.inferno.buildMode.main.BuildMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BuildModeData {
    private ItemStack[] inventory;
    private ItemStack[] armor;
    private static final Gson gson = new Gson();

    public BuildModeData(Player player) {
        saveInventory(player);
    }

    private void saveInventory(Player player) {
        inventory = player.getInventory().getContents();
        armor = player.getInventory().getArmorContents();

        try {
            Connection conn = BuildMode.getInstance().getDatabaseManager().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "REPLACE INTO build_mode_inventories (uuid, inventory, armor) VALUES (?, ?, ?)"
            );
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, gson.toJson(inventory));
            ps.setString(3, gson.toJson(armor));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void restoreInventory(Player player) {
        player.getInventory().setContents(inventory);
        player.getInventory().setArmorContents(armor);
    }
}
