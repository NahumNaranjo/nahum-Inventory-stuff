package dev.nahum.nahumInventoryStuff;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EcCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        Player viewer;
        if(sender instanceof Player) {
            viewer = (Player) sender;
        }   else {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        if(args.length == 0){
            if(!sender.hasPermission("nahum.echest.self") && !sender.hasPermission("nahum.echest")){
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            viewer.openInventory(viewer.getEnderChest());
            return true;
        }
        String option = args[0];
        if (args.length == 1) {
            if(!sender.hasPermission("nahum.echest.other") && !sender.hasPermission("nahum.echest")){
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            OfflinePlayer offlinePlayer = OfflinePlayerSync.getPlayer(option, sender);
            if (offlinePlayer != null) {
                EchestTools.seeEchest(offlinePlayer, viewer, sender);
                return true;
            }
            sender.sendMessage(ChatColor.RED + "That player does not exist.");
            return true;
        }
        OfflinePlayer offlinePlayer = OfflinePlayerSync.getPlayer(args[1], sender);
        if(offlinePlayer == null){
            sender.sendMessage(ChatColor.RED + "That player does not exist.");
            return true;
        }
        switch (option) {
            case "clear":
                if(!sender.hasPermission("nahum.echest") && !sender.hasPermission("nahum.enderchesttools.clear") &&  !sender.hasPermission("nahum.enderchesttools")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                EchestTools.cleanEnderchest(offlinePlayer, sender);
                GoodLogger.info(sender.getName() + " has cleared " +
                        offlinePlayer.getName() + "'s echest using the command /ec with the argument \"clear\"!");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown option: " + option);
                return true;
        }
        return true;
    }
}