package dev.nahum.nahumInventoryStuff;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class EchestToolsCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            return false;
        }

        // Menu logic
        if (args.length == 1) {
            return true;
        }

        // Commands requiring at least 2 arguments
        if (args.length >= 2) {
            String option = args[0].toLowerCase();
            String targetName = args[1];


            // Check if target exists
            OfflinePlayer targetPlayer = OfflinePlayerSync.getPlayer(targetName);
            if (targetPlayer == null) {
                return true;
            }

            File playerData = PathManager.getPlayerFile(targetPlayer.getUniqueId());

            if (!playerData.exists()) {
                sender.sendMessage(ChatColor.RED + targetName + "'s player data does not exist!");
                return true;
            }
            switch (option) {
                case "see":
                    if (!sender.hasPermission("nahum.enderchesttools.see") && !sender.hasPermission("nahum.enderchesttools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    Player viewer;
                    if (sender instanceof Player) {
                        viewer = (Player) sender;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                        return true;
                    }
                    NahumInventoryStuff.addToAdminWatchList(viewer);
                    seeEchest(targetPlayer, viewer, sender);
                    sender.sendMessage(ChatColor.GREEN + "Viewing " + targetPlayer.getName() + "'s ender chest!");
                    GoodLogger.info(sender.getName() + " is looking at " +
                            targetPlayer.getName() + "'s echest using the command /enderchesttools with the argument \"see\"!");
                    break;
                case "edit":
                    if (!sender.hasPermission("nahum.enderchesttools.edit") && !sender.hasPermission("nahum.enderchesttools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (sender instanceof Player) {
                        viewer = (Player) sender;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                        return true;
                    }
                    NahumInventoryStuff.addToIsEditingList(viewer, targetPlayer);
                    editEchest(targetPlayer, viewer, sender);
                    sender.sendMessage(ChatColor.GREEN + "Viewing " + targetPlayer.getName() + "'s ender chest!");
                    GoodLogger.info(sender.getName() + " is editing " + targetPlayer.getName() + "'s ender chest!");
                    break;

                case "transfer":
                    if (!sender.hasPermission("nahum.enderchesttools.transfer") && !sender.hasPermission("nahum.enderchesttools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /echesttools transfer <recipient> <giver>");
                        return true;
                    }

                    String giverName = args[2];
                    OfflinePlayer giverPlayer = OfflinePlayerSync.getPlayer(giverName);
                    if (giverPlayer != null) {
                        transferEchest(targetPlayer, giverPlayer, sender, 1);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Couldn't get " + giverName + "'s OfflinePlayer!");
                        return true;
                    }
                    GoodLogger.info(sender.getName() + " transfered " +
                            targetPlayer.getName() + "'s echest to " + giverName + "'s using the command /enderchesttools with the argument \"transfer\"!");
                    break;
                case "swap":
                    if (!sender.hasPermission("nahum.enderchesttools.swap") && !sender.hasPermission("nahum.enderchesttools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /echesttools transfer <recipient> <giver>");
                        return true;
                    }


                    giverName = args[2];
                    giverPlayer = OfflinePlayerSync.getPlayer(giverName);
                    if (giverPlayer != null) {
                        transferEchest(targetPlayer, giverPlayer, sender, 0);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Couldn't get " + giverName + "'s OfflinePlayer!");
                        return true;
                    }
                    GoodLogger.info(sender.getName() + " swaped " +
                            targetPlayer.getName() + "'s echest to " + giverName + "'s using the command /enderchesttools with the argument \"transfer\"!");
                    break;

                case "clear":
                    if (!sender.hasPermission("nahum.enderchesttools.clear") && !sender.hasPermission("nahum.enderchesttools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    cleanEnderchest(targetPlayer, sender);
                    GoodLogger.info(sender.getName() + " has cleared " +
                            targetPlayer.getName() + "'s echest using the command /enderchesttools with the argument \"clear\"!");
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        String currentInput = null;
        if (args.length == 1) {
            if (sender.hasPermission("nahum.enderchesttools")) {
                completions.add("clear");
                completions.add("swap");
                completions.add("transfer");
                completions.add("see");
                completions.removeIf(option -> !option.toLowerCase().startsWith(args[0].toLowerCase()));
                return completions;
            }
            if (sender.hasPermission("nahum.enderchesttools.see")) {
                completions.add("see");
            }
            if (sender.hasPermission("nahum.enderchesttools.swap")) {
                completions.add("swap");
            }
            if (sender.hasPermission("nahum.enderchesttools.transfer")) {
                completions.add("transfer");
            }
            if (sender.hasPermission("nahum.enderchesttools.clear")) {
                completions.add("clear");
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