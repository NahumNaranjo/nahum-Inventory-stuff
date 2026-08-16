package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.ListTag;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class NahumInventoryStuff extends JavaPlugin {

    private final static String date = "14/August/26";
    private static NahumInventoryStuff plugin;
    private static List<UUID> adminWatchList = new ArrayList<>();
    private static List<UUID> playerWatchList = new ArrayList<>();
    private static Map<UUID, UUID> isEditingList = new HashMap<>();
    private static Instant lastTimeBackuped;

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

    static public boolean isOnAdminWatchList(Object object){
        return adminWatchList.contains(DataParser.getUuidFromObject(object));
    }

    static public void addToAdminWatchList(Object object){
        adminWatchList.add(DataParser.getUuidFromObject(object));
    }
    static public void removeFromAdminWatchList(Object object){
        adminWatchList.remove(DataParser.getUuidFromObject(object));
    }

    static public boolean isOnIsEditingList(Object object){
        return isEditingList.containsKey(DataParser.getUuidFromObject(object));
    }

    static public void addToIsEditingList(Object admin, Object victim){
        isEditingList.put(DataParser.getUuidFromObject(admin), DataParser.getUuidFromObject(victim));
    }

    static public UUID getVictimFromIsEditingList(Object admin){
        return isEditingList.get(DataParser.getUuidFromObject(admin));
    }

    static public void removeFromIsEditingList(Object admin, Object victim){
        if(victim == null) {
            isEditingList.remove(DataParser.getUuidFromObject(admin), getVictimFromIsEditingList(admin));
            return;
        }
        isEditingList.remove(DataParser.getUuidFromObject(admin), DataParser.getUuidFromObject(victim));
    }



    @Override
    public void onEnable() {
        plugin = this;
        getInstance().saveDefaultConfig();
        ConfigManager.load();

        int pluginBStatsID = 33407;
        Metrics metrics = new Metrics(getInstance(), pluginBStatsID);

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

        getServer().getPluginManager().registerEvents(new InventorySecurityWatcher(), this);


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
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Check if auto-backup is enabled
                    if (!ConfigManager.hasAutoBackup()) {
                        GoodLogger.debug("Auto-backup is disabled in config");
                        return;
                    }

                    GoodLogger.debug("=== Backup Check ===");
                    GoodLogger.debug("Current time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    GoodLogger.debug("Fixed mode: " + ConfigManager.isFixedMode());

                    // Initialize lastBackupTime if null
                    if (lastTimeBackuped == null) {
                        GoodLogger.info("Initializing backup system...");
                        performBackup();
                        lastTimeBackuped = Instant.now();
                        GoodLogger.debug("Initial backup completed at: " + Instant.now());
                        return;
                    }

                    if (ConfigManager.isFixedMode()) {
                        handleFixedBackup();
                    } else {
                        handleDurationBackup();
                    }
                } catch (Exception e) {
                    GoodLogger.error("Backup scheduler error: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            private void handleFixedBackup() {
                GoodLogger.debug("--- Fixed Mode Check ---");

                if(ConfigManager.hasAutoDelete()) {
                    deleteOldBackups(FileManager.getBackupFolder());
                }
                LocalTime now = LocalTime.now();
                LocalTime scheduled = ConfigManager.getParsedSchedule();

                if (scheduled == null) {
                    GoodLogger.warn("Fixed mode enabled but schedule is invalid!");
                    return;
                }

                GoodLogger.debug("Current time: " + now.format(DateTimeFormatter.ofPattern("HH:mm")));
                GoodLogger.debug("Scheduled time: " + scheduled.format(DateTimeFormatter.ofPattern("HH:mm")));
                GoodLogger.debug("Time difference: " +
                        Math.abs(Duration.between(now, scheduled).getSeconds()) + " seconds");

                // Check if it's time (within 30 seconds window)
                long secondsDifference = Math.abs(Duration.between(now, scheduled).getSeconds());
                boolean isTime = secondsDifference < 30;

                GoodLogger.debug("Is it time? " + isTime);

                if (isTime) {
                    // Check if we haven't backed up today
                    LocalDate lastBackupDate = LocalDate.ofInstant(lastTimeBackuped, ZoneId.systemDefault());
                    LocalDate today = LocalDate.now();

                    GoodLogger.debug("Last backup date: " + lastBackupDate);
                    GoodLogger.debug("Today's date: " + today);
                    GoodLogger.debug("Already backed up today? " + !lastBackupDate.isBefore(today));

                    if (lastBackupDate.isBefore(today)) {
                        GoodLogger.info("✓ Performing scheduled backup at " + now.format(DateTimeFormatter.ofPattern("HH:mm")));
                        if(ConfigManager.hasAutoDelete()) {
                            deleteOldBackups(FileManager.getBackupFolder());
                        }
                        performBackup();
                        lastTimeBackuped = Instant.now();
                        GoodLogger.debug("Backup completed. Next scheduled backup: tomorrow at " +
                                scheduled.format(DateTimeFormatter.ofPattern("HH:mm")));
                    } else {
                        GoodLogger.debug("Already backed up today. Skipping...");
                        // Show time until next backup
                        LocalDateTime nextBackup = LocalDateTime.of(today.plusDays(1), scheduled);
                        Duration untilNext = Duration.between(LocalDateTime.now(), nextBackup);
                        GoodLogger.debug("Next backup in: " + formatDuration(untilNext.getSeconds()));
                    }
                } else {
                    // Show time until next backup if within next 24 hours
                    LocalDateTime nextBackup;
                    if (now.isBefore(scheduled)) {
                        nextBackup = LocalDateTime.of(LocalDate.now(), scheduled);
                    } else {
                        nextBackup = LocalDateTime.of(LocalDate.now().plusDays(1), scheduled);
                    }
                    Duration untilNext = Duration.between(LocalDateTime.now(), nextBackup);
                    GoodLogger.debug("Next backup in: " + formatDuration(untilNext.getSeconds()));
                }
            }

            private void handleDurationBackup() {
                GoodLogger.debug("--- Duration Mode Check ---");

                Duration interval = ConfigManager.getParsedLapse();

                if (interval == null) {
                    GoodLogger.warn("No valid backup interval! Using default: 30 minutes");
                    interval = Duration.ofMinutes(30);
                }

                long elapsedSeconds = Duration.between(lastTimeBackuped, Instant.now()).getSeconds();
                long intervalSeconds = interval.getSeconds();

                GoodLogger.debug("Last backup: " + Instant.ofEpochSecond(lastTimeBackuped.getEpochSecond())
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                GoodLogger.debug("Elapsed time: " + formatDuration(elapsedSeconds));
                GoodLogger.debug("Backup interval: " + formatDuration(intervalSeconds));
                GoodLogger.debug("Time until next backup: " +
                        (elapsedSeconds < intervalSeconds ? formatDuration(intervalSeconds - elapsedSeconds) : "NOW"));

                if (elapsedSeconds >= intervalSeconds) {
                    GoodLogger.info("✓ Performing duration backup (elapsed: " +
                            formatDuration(elapsedSeconds) + " / interval: " +
                            formatDuration(intervalSeconds) + ")");
                    if(ConfigManager.hasAutoDelete()){
                        deleteOldBackups(FileManager.getBackupFolder());
                    }
                    performBackup();
                    lastTimeBackuped = Instant.now();
                    GoodLogger.debug("Backup completed. Next backup in: " + formatDuration(intervalSeconds));
                } else {
                    GoodLogger.debug("Not yet time. Next backup in: " +
                            formatDuration(intervalSeconds - elapsedSeconds));
                }
            }

            private String formatDuration(long seconds) {
                if (seconds < 0) return "0s";

                long days = seconds / 86400;
                long hours = (seconds % 86400) / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;

                StringBuilder result = new StringBuilder();
                if (days > 0) result.append(days).append("d ");
                if (hours > 0) result.append(hours).append("h ");
                if (minutes > 0) result.append(minutes).append("m ");
                if (secs > 0 || result.length() == 0) result.append(secs).append("s");

                return result.toString().trim();
            }
        }.runTaskTimer(getInstance(), 0L, 20L);
    }

    public static void deleteOldBackups(File folder) {
        LocalDate maxAge = ConfigManager.getMaxAge(null);
        try (Stream<Path> stream = Files.list(folder.toPath())) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

                            LocalDate age = attr.creationTime().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();

                            if(age.isBefore(maxAge)) {
                                GoodLogger.debug("\nDeleting old backup: " + path.toAbsolutePath() + "\nBecause is older than: " + maxAge.toString() + " with an age of " + age.toString());
                                Files.deleteIfExists(path);
                            }
                            if(path.toFile().exists()) {
                                LocalDate ageFromName = FileManager.getAgeFromName(path.toFile().getName().replace(".nahumbackup", ""));
                                if(ageFromName.isBefore(maxAge)) {
                                    GoodLogger.debug("\nDeleting old backup: " + path.toAbsolutePath() + "\nBecause is older than: " + maxAge.toString() + " with an age of " + ageFromName.toString());
                                    Files.deleteIfExists(path);
                                }
                            }
                            GoodLogger.debug("\nNot deleting old backup: " + path.toAbsolutePath() + "\nBecause is younger than: " + maxAge.toString() + " with an age of " + age.toString());
                        } catch (IOException e) {
                            System.err.println("Could not read attributes for: " + path.getFileName());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        ConfigManager.save();
    }


    public void performBackup(){
        GoodLogger.info("Performing backup...");
        Map<UUID, LinkedList<ListTag>> onlineUsers = FileManager.fetchAllOnlineUserData();
        FileManager.writeBackup(null, onlineUsers);
    }
}