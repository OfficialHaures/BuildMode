package nl.inferno.buildMode.listeners;

import nl.inferno.buildMode.BuildMode;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class BuildModeListener implements Listener {
    private final BuildMode plugin;

    public BuildModeListener(BuildMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("buildmode.see")) {
            plugin.getBuildModePlayers().forEach(buildModePlayer ->
                player.hidePlayer(plugin, buildModePlayer));
        }

        if (plugin.wasInBuildMode(player.getUniqueId())) {
            plugin.enableBuildMode(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInBuildMode(player)) {
            plugin.saveBuildModeState(player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInBuildMode(player)) {
            ItemStack item = event.getItem();
            if (item != null && item.equals(getBuildModeCompass())) {
                player.setGameMode(player.getGameMode() == GameMode.CREATIVE
                    ? GameMode.SPECTATOR
                    : GameMode.CREATIVE);
                player.sendMessage("§aGamemode switched!");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (plugin.isInBuildMode(player)) {
                // Prevent moving build mode tools
                if (isBuildModeTool(event.getCurrentItem())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.isInBuildMode(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (plugin.isInBuildMode(attacker)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.isInBuildMode(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInBuildMode(player)) {
            if (isBuildModeTool(event.getItemDrop().getItemStack())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInBuildMode(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isBuildModeTool(ItemStack item) {
        return item != null && item.hasItemMeta() &&
               item.getItemMeta().hasLore() &&
               item.getItemMeta().getLore().contains("§7Build Mode Tool");
    }

    public static ItemStack getBuildModeCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName("§6Mode Switcher");
        meta.setLore(Arrays.asList(
            "§7Build Mode Tool",
            "§eClick to switch between",
            "§eCreative and Spectator mode"
        ));
        compass.setItemMeta(meta);
        return compass;
    }
}

