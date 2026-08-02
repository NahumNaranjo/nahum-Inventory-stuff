package dev.nahum.nahumInventoryStuff;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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

        // Check if player is online
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player " + playerName + " is not online!");
            return true;
        }

        String playerDisplayName = targetPlayer.getName();

        switch (option) {
            case "all":
                targetPlayer.getInventory().clear();
                targetPlayer.getEnderChest().clear();
                sender.sendMessage(ChatColor.GREEN + "Successfully cleared all storage for " + playerDisplayName + "!");
                break;

            case "echest":
                targetPlayer.getEnderChest().clear();
                sender.sendMessage(ChatColor.GREEN + "Successfully cleared " + playerDisplayName + "'s ender chest!");
                break;

            case "inventory":
                targetPlayer.getInventory().clear();
                sender.sendMessage(ChatColor.GREEN + "Successfully cleared " + playerDisplayName + "'s inventory!");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown option: " + option);
                sender.sendMessage(ChatColor.RED + "Valid options: all, echest, inventory");
                return false;
        }

        return true;
    }
}