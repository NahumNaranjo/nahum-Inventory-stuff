package dev.nahum.nahumInventoryStuff;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OfflinePlayerSync {
    public static OfflinePlayer getPlayer(String name) {
        GoodLogger.debugSection("OfflinePlayerSync.getPlayer");

        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        GoodLogger.debugPlayer("Resolved", player);

        if (!player.isOnline() && !player.hasPlayedBefore()) {
            GoodLogger.warn("Player lookup failed: " + name + " has never played on this server");
            return null;
        }

        GoodLogger.debug("Player lookup success: " + player.getName() + " online=" + player.isOnline());
        return player;
    }

    public static List<OfflinePlayer> getAllPlayers() {
        List<OfflinePlayer> players = new ArrayList<>();
        Collections.addAll(players, Bukkit.getOfflinePlayers());
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(Bukkit.getOfflinePlayer(player.getName()));
        }
        return players;
    }

    public static boolean isOnline(OfflinePlayer player) {
        boolean online = player.getPlayer() != null;
        GoodLogger.debug("isOnline(" + (player == null ? "null" : player.getName()) + ") -> " + online);
        return online;
    }
}
