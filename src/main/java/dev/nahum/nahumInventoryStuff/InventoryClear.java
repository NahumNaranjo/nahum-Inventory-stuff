package dev.nahum.nahumInventoryStuff;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class InventoryClear implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /inventoryclean <option> <username>");
            sender.sendMessage(ChatColor.RED + "Options: all, echest, inventory");
            return true;
        }

        String option = args[0].toLowerCase();
        String playerName = args[1];

        // Check if player exists
        OfflinePlayer targetPlayer  = OfflinePlayerSync.getPlayer(playerName, sender);
        if (targetPlayer == null) {
            return true;
        }

        String playerDisplayName = targetPlayer.getName();

        switch (args[0].toLowerCase()) {
            case "all":
                InventoryTools.cleanInventory(targetPlayer, sender);
                EchestTools.cleanEnderchest(targetPlayer, sender);
                sender.sendMessage(ChatColor.GREEN + "Successfully cleared all storage for " + playerDisplayName + "!");
                break;

            case "echest":
                EchestTools.cleanEnderchest(targetPlayer, sender);
                break;

            case "inventory":
                InventoryTools.cleanInventory(targetPlayer, sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown option: " + option);
                sender.sendMessage(ChatColor.RED + "Valid options: all, echest, inventory");
                return false;
        }

        return true;
    }
}