package dev.nahum.nahumInventoryStuff;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class NahumInventoryStuff extends JavaPlugin {

    private final static String date = "14/August/26";
    private static NahumInventoryStuff plugin;

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

    @Override
    public void onEnable() {
        plugin = this;
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
            GoodLogger.info("Looking for new versions...");
            UpdateChecker.setUp();
            GoodLogger.info("Fetched new versions!");
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
                    GoodLogger.info("Newest version is already installed!");
                }
            });
        }).exceptionally(ex -> {
            GoodLogger.error(ex.getMessage());
            return null;
        });
    }

    @Override
    public void onDisable() {

    }

}