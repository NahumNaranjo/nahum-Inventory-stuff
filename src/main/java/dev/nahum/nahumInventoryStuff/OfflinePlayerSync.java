package dev.nahum.nahumInventoryStuff;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class OfflinePlayerSync {
    public static OfflinePlayer getPlayer(String name, CommandSender sender){
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if(!player.isOnline() && !player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "Player " + name + " has never played here!");
            return null;
        }
        return player;
    }
    public static boolean isOnline(OfflinePlayer player){
        return player.getPlayer() != null;
    }
}