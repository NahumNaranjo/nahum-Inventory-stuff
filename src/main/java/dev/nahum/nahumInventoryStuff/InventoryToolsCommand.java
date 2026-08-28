package dev.nahum.nahumInventoryStuff;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class InventoryToolsCommand implements TabExecutor {
    static final String[] features = {"see", "edit", "swap", "transfer", "clear"};

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
            OfflinePlayer targetPlayer = OfflinePlayerSync.getPlayer(targetName);
            if (targetPlayer == null) {
                return true;
            }

            switch (option) {
                case "see":
                    if (!sender.hasPermission("nahum.inventorytools.see") && !sender.hasPermission("nahum.inventorytools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                        return true;
                    }

                    Player viewer = (Player) sender;
                    NahumInventoryStuff.addToAdminWatchList(viewer);
                    seeInventory(targetPlayer, viewer, sender);
                    GoodLogger.info(sender.getName() + " is looking at " +
                            targetPlayer.getName() + "'s inventory using the command /inventorytools with the argument \"see\"!");
                    break;
                case "edit":
                    if (!sender.hasPermission("nahum.inventorytools.edit") && !sender.hasPermission("nahum.inventorytools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                        return true;
                    }
                    viewer = (Player) sender;
                    NahumInventoryStuff.addToIsEditingList(viewer, targetPlayer);
                    GoodLogger.debug("Added " + viewer.getUniqueId() + " to is editing");
                    editInventory(targetPlayer, viewer, sender);
                    GoodLogger.info(sender.getName() + " is editing at " +
                            targetPlayer.getName() + "'s inventory using the command /inventorytools with the argument \"edit\"!");
                    break;

                case "transfer":
                    if (!sender.hasPermission("nahum.inventorytools.transfer") && !sender.hasPermission("nahum.inventorytools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /inventorytools transfer <recipient> <giver>");
                        return true;
                    }

                    OfflinePlayer giverPlayer = OfflinePlayerSync.getPlayer(args[2]);
                    if (giverPlayer == null) {
                        sender.sendMessage(ChatColor.RED + "Couldn't get " + args[2] + "'s OfflinePlayer!");
                        return true;
                    }

                    transferInventory(targetPlayer, giverPlayer, sender, (byte) 1);
                    GoodLogger.info(sender.getName() + " transfered " +
                            targetPlayer.getName() + "'s inventory to " + giverPlayer.getName() +
                            "'s using the command /inventorytools with the argument \"transfer\"!");
                    break;

                case "clear":
                    if (!sender.hasPermission("nahum.inventorytools.clear") && !sender.hasPermission("nahum.inventorytools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    cleanInventory(targetPlayer, sender);
                    GoodLogger.info(sender.getName() + " has cleared " +
                            targetPlayer.getName() + "'s inventory using the command /inventorytools with the argument \"clear\"!");
                    break;

                case "swap":
                    if (!sender.hasPermission("nahum.inventorytools.swap") && !sender.hasPermission("nahum.inventorytools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /inventorytools transfer <recipient> <giver>");
                        return true;
                    }
                    OfflinePlayer swapPlayer = OfflinePlayerSync.getPlayer(args[2]);
                    if (swapPlayer == null) {
                        sender.sendMessage(ChatColor.RED + "Couldn't get " + args[2] + "'s OfflinePlayer!");
                        return true;
                    }
                    transferInventory(targetPlayer, swapPlayer, sender, (byte) 0);
                    GoodLogger.info(sender.getName() + " transfered " +
                            targetPlayer.getName() + "'s inventory to " + swapPlayer.getName() +
                            "'s using the command /inventorytools with the argument \"transfer\"!");
                    return true;


                default:
                    sender.sendMessage(ChatColor.RED + "Unknown option: " + option);
                    sender.sendMessage(ChatColor.RED + "Valid options: see, transfer, clear");
                    return false;
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        String currentInput = null;
        if (args.length == 1) {
            if (sender.hasPermission("nahum.inventorytools")) {
                completions.addAll(Arrays.asList(features));
                completions.removeIf(option -> !option.toLowerCase().startsWith(args[0].toLowerCase()));
                return completions;
            }
            if (sender.hasPermission("nahum.inventorytools.see")) {
                completions.add("see");
            }
            if (sender.hasPermission("nahum.inventorytools.swap")) {
                completions.add("swap");
            }
            if (sender.hasPermission("nahum.inventorytools.transfer")) {
                completions.add("transfer");
            }
            if (sender.hasPermission("nahum.inventorytools.clear")) {
                completions.add("clear");
            }
            if (sender.hasPermission("nahum.inventorytools.edit")) {
                completions.add("edit");
            }
            currentInput = args[0].toLowerCase();
        }
        if (args.length == 2 || args.length == 3) {
            List<String> players = new LinkedList<>();
            for (OfflinePlayer player : OfflinePlayerSync.getAllPlayers()) {
                players.add(player.getName());
            }
            Collections.sort(players);
            players.removeIf(option -> !option.toLowerCase().startsWith(args.length == 2 ? args[1] : args[2]));
            return players;
        }
        if (currentInput == null) {
            return new ArrayList<>();
        }
        String unmatched = currentInput;
        completions.removeIf(option -> !option.toLowerCase().startsWith(unmatched));
        return completions;
    }
}