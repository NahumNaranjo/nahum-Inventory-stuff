package dev.nahum.nahumInventoryStuff;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        SenderLogger toSender = new  SenderLogger(sender);
        InvTools tools = new InvTools(sender);
        Player viewer;
        if (sender instanceof Player) {
            viewer = (Player) sender;
        } else {
            toSender.error("Only players can use this command.");
            return true;
        }
        if (args.length == 0) {
            if (!sender.hasPermission("nahum.inv.self") && !sender.hasPermission("nahum.inv")) {
                toSender.error("You do not have permission to use this command.");
                return true;
            }
            viewer.openInventory(viewer.getInventory());
            return true;
        }

        String option = args[0];
        if (args.length == 1) {
            if (!sender.hasPermission("nahum.inv.other") && !sender.hasPermission("nahum.inv")) {
                toSender.error("You do not have permission to use this command.");
                return true;
            }
            OfflinePlayer offlinePlayer = OfflinePlayerSync.getPlayer(option);
            if (offlinePlayer != null) {
                tools.seeInventory(offlinePlayer, viewer);
                return true;
            }
            toSender.error("That player does not exist.");
            return true;
        }
        OfflinePlayer offlinePlayer = OfflinePlayerSync.getPlayer(args[1]);
        if (offlinePlayer == null) {
            toSender.error("That player does not exist.");
            return true;
        }
        switch (option) {
            case "clear":
                if (!sender.hasPermission("nahum.inv") && !sender.hasPermission("nahum.inventorytools.clear") && !sender.hasPermission("nahum.inventorytools")) {
                    toSender.error("You do not have permission to use this command.");
                    return true;
                }
                if (!sender.hasPermission("nahum.invcommand.see") && !sender.hasPermission("nahum.inventorytools")) {
                    toSender.error("You do not have permission to use this command!");
                    return true;
                }
                tools.cleanInventory(offlinePlayer);
                GoodLogger.info(sender.getName() + " has cleared " +
                        offlinePlayer.getName() + "'s inventory using the command /inv with the argument \"clear\"!");
                break;
            default:
                toSender.error("Unknown option: " + option);
                return true;
        }
        return true;
    }
}