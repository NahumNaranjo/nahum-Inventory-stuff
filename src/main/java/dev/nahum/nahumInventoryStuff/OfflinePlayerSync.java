package dev.nahum.nahumInventoryStuff;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class OfflinePlayerSync {
    public static OfflinePlayer getPlayer(String name, CommandSender sender){
        GoodLogger.debugSection("OfflinePlayerSync.getPlayer");
        GoodLogger.debug("Resolving player name='" + name + "' requestedBy=" + sender.getName());

        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        GoodLogger.debugPlayer("Resolved", player);

        if(!player.isOnline() && !player.hasPlayedBefore()) {
            GoodLogger.warn("Player lookup failed: " + name + " has never played on this server");
            sender.sendMessage(ChatColor.RED + "Player " + name + " has never played here!");
            return null;
        }

        GoodLogger.debug("Player lookup success: " + player.getName() + " online=" + player.isOnline());
        return player;
    }

    public static boolean isOnline(OfflinePlayer player){
        boolean online = player.getPlayer() != null;
        GoodLogger.debug("isOnline(" + (player == null ? "null" : player.getName()) + ") -> " + online);
        return online;
    }
}
