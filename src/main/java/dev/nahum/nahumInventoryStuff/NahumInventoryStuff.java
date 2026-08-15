package dev.nahum.nahumInventoryStuff;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class NahumInventoryStuff extends JavaPlugin {

    private final static String date = "14/August/26";
    private static NahumInventoryStuff plugin;
    private static List<UUID> adminWatchList = new ArrayList<>();

    public static boolean onDebug = false;

    public static String getCredits(){
        return "NahumInventoryStuff :D\n" +
                "Author: Nahum Naranjo\n" +
                "Version: " + UpdateChecker.getCurrentVersion() + "\n" +
                "Date of publishing: " + date;
    }
    static public NahumInventoryStuff getInstance() {return plugin;}
    static public Logger getLog() {return GoodLogger.getLog();}
    static public Logger getGoodLogger() {return plugin != null ? plugin.getLogger() : Bukkit.getLogger();}
    static public boolean getOnDebug() {return onDebug;}
    static public void setOnDebug(boolean newDebug) {onDebug = newDebug;}

    static public boolean isOnAdminWatchList(UUID uuid){return adminWatchList.contains(uuid);}
    static public boolean isOnAdminWatchList(Player player){return adminWatchList.contains(player.getUniqueId());}
    static public boolean isOnAdminWatchList(OfflinePlayer player){return adminWatchList.contains(player.getUniqueId());}

    static public void addToAdminWatchList(UUID uuid){adminWatchList.add(uuid);}
    static public void addToAdminWatchList(Player player){adminWatchList.add(player.getUniqueId());}
    static public void addToAdminWatchList(OfflinePlayer player){adminWatchList.add(player.getUniqueId());}

    static public void removeFromAdminWatchList(UUID uuid){adminWatchList.remove(uuid);}
    static public void removeFromAdminWatchList(Player player){adminWatchList.remove(player.getUniqueId());}
    static public void removeFromAdminWatchList(OfflinePlayer player){adminWatchList.remove(player.getUniqueId());}



    @Override
    public void onEnable() {
        plugin = this;
        getInstance().saveDefaultConfig();
        ConfigManager.load();

        if(ConfigManager.getConfig("onDebug") ==  null){
            onDebug = false;
        } else {
            onDebug = (Boolean)ConfigManager.getConfig("onDebug");
        }

        GoodLogger.info("Debug mode: " + (onDebug ? "ON" : "OFF") + " (toggle with /nahumstuff debug)");
        GoodLogger.debug("Registering command executors...");

        this.getCommand("echesttools").setExecutor(new EchestTools());
        this.getCommand("inventorytools").setExecutor(new InventoryTools());
        this.getCommand("inventoryclear").setExecutor(new InventoryClear());
        this.getCommand("ec").setExecutor(new EcCommand());
        this.getCommand("inv").setExecutor(new InvCommand());
        this.getCommand("nahumstuff").setExecutor(new NahumInventoryStuffCommand());

        GoodLogger.debug("World folder: " + Bukkit.getWorlds().getFirst().getWorldFolder().getAbsolutePath());
        GoodLogger.debug("Plugin data folder: " + getDataFolder().getAbsolutePath());



        CompletableFuture.supplyAsync(() -> {
            GoodLogger.debug("Looking for new versions...");
            UpdateChecker.setUp();
            GoodLogger.debug("Fetched new versions!");
            return UpdateChecker.getCurrentVersion().equals(UpdateChecker.getLatestVersion());
        }).thenAccept(result -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (result != true) {
                    GoodLogger.info(
                            "\nA new NahumInventoryStuff version is available!\n" +
                            "Current version: " + UpdateChecker.getCurrentVersion() + "\n" +
                            "Newest version: " + UpdateChecker.getLatestVersion() + "\n" +
                            "Updating is recommended ;D\n" +
                            "Download links: \n" +
                            UpdateChecker.getAvailableLinks()
                    );
                } else {
                    GoodLogger.info("Loaded: " + UpdateChecker.getCurrentVersion());
                    GoodLogger.debug("Latest version: " + UpdateChecker.getLatestVersion());
                    GoodLogger.debug("Latest date: " + UpdateChecker.getLatestDate());
                    GoodLogger.debug("Newest version is already installed!");
                }
            });
        }).exceptionally(ex -> {
            GoodLogger.error(ex.getMessage());
            return null;
        });

    }

    @Override
    public void onDisable() {
        ConfigManager.save();
    }

}