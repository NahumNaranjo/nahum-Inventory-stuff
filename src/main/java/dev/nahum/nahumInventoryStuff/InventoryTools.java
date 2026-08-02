package dev.nahum.nahumInventoryStuff;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class InventoryTools implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        // Check for "menu" (single argument)
        if (args.length == 1) {
            // menu logic
            return true;
        }

        // Commands requiring at least 2 arguments
        if (args.length >= 2) {
            String option = args[0].toLowerCase();
            String targetName = args[1];

            // Check if target is online
            Player targetPlayer = Bukkit.getPlayerExact(targetName);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player " + targetName + " is not online!");
                return true;
            }

            switch (option) {
                case "see":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                        return true;
                    }

                    Player viewer = (Player) sender;
                    viewer.openInventory(targetPlayer.getInventory());
                    sender.sendMessage(ChatColor.GREEN + "Viewing " + targetPlayer.getName() + "'s inventory!");
                    break;

                case "transfer":
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /inventorytools transfer <recipient> <giver>");
                        return true;
                    }

                    String giverName = args[2];
                    Player giverPlayer = Bukkit.getPlayerExact(giverName);

                    if (giverPlayer == null) {
                        sender.sendMessage(ChatColor.RED + "Giver " + giverName + " is not online!");
                        return true;
                    }

                    // Copy inventory contents
                    ItemStack[] giverInventory = giverPlayer.getInventory().getContents();
                    targetPlayer.getInventory().setContents(giverInventory);

                    sender.sendMessage(ChatColor.GREEN + "Successfully transferred " + giverPlayer.getName() +
                            "'s inventory to " + targetPlayer.getName() + "'s!");
                    break;

                case "clear":
                    targetPlayer.getInventory().clear();
                    sender.sendMessage(ChatColor.GREEN + "Successfully cleared " + targetPlayer.getName() + "'s inventory!");
                    break;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown option: " + option);
                    sender.sendMessage(ChatColor.RED + "Valid options: see, transfer, clear");
                    return false;
            }
            return true;
        }

        return false;
    }
}