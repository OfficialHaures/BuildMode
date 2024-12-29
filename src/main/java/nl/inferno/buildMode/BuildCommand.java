package nl.inferno.buildMode;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandExecutor {
    private final BuildMode plugin;

    public BuildCommand(BuildMode plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("buildmode.use")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (plugin.isInBuildMode(player)) {
            plugin.disableBuildMode(player);
            player.sendMessage("§aBuild mode disabled!");
        } else {
            plugin.enableBuildMode(player);
            player.sendMessage("§aBuild mode enabled!");
        }

        return true;
    }
}
