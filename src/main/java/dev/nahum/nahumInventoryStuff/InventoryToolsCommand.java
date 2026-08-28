package dev.nahum.nahumInventoryStuff;

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
        InvTools invTools = new InvTools(sender);
        SenderLogger toSender = new SenderLogger(sender);

        if (args.length == 0) {
            return false;
        }

        // Check for "menu" (single argument)
        if (args.length == 1) {
            // menu logic
            return true;
        }

        // Commands requiring at least 2 arguments
        String option = args[0].toLowerCase();
        String targetName = args[1];

        Player viewer = null;
        if(sender instanceof Player) {
            viewer = (Player) sender;
        }

        // Check if target is online
        OfflinePlayer targetPlayer = OfflinePlayerSync.getPlayer(targetName);
        if (targetPlayer == null) {
            return true;
        }

        switch (option) {
            case "see":
                if (!sender.hasPermission("nahum.inventorytools.see") && !sender.hasPermission("nahum.inventorytools")) {
                    toSender.error("You do not have permission to use this command!");
                    return true;
                }
                if (viewer == null) {
                    toSender.error("You must be a player to use this command!");
                    return true;
                }

                NahumInventoryStuff.addToAdminWatchList(viewer);
                invTools.seeInventory(targetPlayer, viewer);
                GoodLogger.info(sender.getName() + " is looking at " +
                        targetPlayer.getName() + "'s inventory using the command /inventorytools with the argument \"see\"!");
                break;
            case "edit":
                if (!sender.hasPermission("nahum.inventorytools.edit") && !sender.hasPermission("nahum.inventorytools")) {
                    toSender.error("You do not have permission to use this command!");
                    return true;
                }
                if (viewer == null) {
                    toSender.error("You must be a player to use this command!");
                    return true;
                }

                NahumInventoryStuff.addToIsEditingList(viewer, targetPlayer);
                GoodLogger.debug("Added " + viewer.getUniqueId() + " to isEditing");
                invTools.editInventory(targetPlayer, viewer);
                GoodLogger.info(sender.getName() + " is editing at " +
                        targetPlayer.getName() + "'s inventory using the command /inventorytools with the argument \"edit\"!");
                break;

            case "transfer":
                if (!sender.hasPermission("nahum.inventorytools.transfer") && !sender.hasPermission("nahum.inventorytools")) {
                    toSender.error("You do not have permission to use this command!");
                    return true;
                }
                if (args.length < 3) {
                    toSender.error("Usage: /inventorytools transfer <recipient> <giver>");
                    return true;
                }

                OfflinePlayer giverPlayer = OfflinePlayerSync.getPlayer(args[2]);
                if (giverPlayer == null) {
                    toSender.error("Couldn't get " + args[2] + "'s OfflinePlayer!");
                    return true;
                }

                invTools.transferInventory(targetPlayer, giverPlayer);
                GoodLogger.info(sender.getName() + " transfered " +
                        targetPlayer.getName() + "'s inventory to " + giverPlayer.getName() +
                        "'s using the command /inventorytools with the argument \"transfer\"!");
                break;

            case "clear":
                if (!sender.hasPermission("nahum.inventorytools.clear") && !sender.hasPermission("nahum.inventorytools")) {
                    toSender.error("You do not have permission to use this command!");
                    return true;
                }
                invTools.cleanInventory(targetPlayer);
                GoodLogger.info(sender.getName() + " has cleared " +
                        targetPlayer.getName() + "'s inventory using the command /inventorytools with the argument \"clear\"!");
                break;

            case "swap":
                if (!sender.hasPermission("nahum.inventorytools.swap") && !sender.hasPermission("nahum.inventorytools")) {
                    toSender.error("You do not have permission to use this command!");
                    return true;
                }
                if (args.length < 3) {
                    toSender.error("Usage: /inventorytools transfer <recipient> <giver>");
                    return true;
                }
                OfflinePlayer swapPlayer = OfflinePlayerSync.getPlayer(args[2]);
                if (swapPlayer == null) {
                    toSender.error("Couldn't get " + args[2] + "'s OfflinePlayer!");
                    return true;
                }
                invTools.swapInventory(targetPlayer, swapPlayer);
                GoodLogger.info(sender.getName() + " transfered " +
                        targetPlayer.getName() + "'s inventory to " + swapPlayer.getName() +
                        "'s using the command /inventorytools with the argument \"transfer\"!");
                return true;


            default:
                toSender.warning("Unknown option: " + option);
                toSender.warning("Valid options: see, transfer, clear");
                return false;
        }
        return true;

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