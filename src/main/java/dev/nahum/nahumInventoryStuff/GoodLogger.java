package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.logging.Logger;

public class GoodLogger {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[36m";
    public static final String LIGHTBLUE = "\u001B[94m";
    public static final String ORANGE = "\u001B[38;5;208m";
    public static final String[] Colors = new String[]{
            RESET, RED, ORANGE, GREEN, YELLOW, BLUE, LIGHTBLUE,
    };
    public static Logger logger = getLogger();
    private static final boolean onDebug = (boolean)ConfigManager.getConfigOrDefault("onDebug", false);

    private static Logger getLogger() {
        return NahumInventoryStuff.getInstance() != null ? NahumInventoryStuff.getInstance().getLogger() : Bukkit.getLogger();
    }

    public static Logger getLog() {
        logger = getLogger();
        return logger;
    }

    public static void info(String message) {
        logger.info(BLUE + message + RESET);
    }

    public static void warn(String message) {
        logger.warning(YELLOW + message + RESET);
    }

    public static void error(String message) {
        logger.warning(RED + message + RESET);
    }

    public static void severe(String message) {
        error(message);
    }

    public static void severe(String message, Throwable t) {
        error(message, t);
    }

    public static void error(String message, Throwable throwable) {
        error(message);
        if (throwable != null) {
            error("Exception: " + throwable.getClass().getSimpleName() + " -> " + throwable.getMessage() + "\n" + "Cause: " + throwable.getCause());
            if(onDebug)
                throwable.printStackTrace();
        }
    }

    public static void success(String message) {
        logger.info(GREEN + message + RESET);
    }

    public static void dev(String message) {
        if((boolean)ConfigManager.getConfigOrDefault("onDev", false)){
            logger.info(ORANGE + "DEV: " + message + RESET);
        }
    }

    public static void time(String message) {
        if((boolean)ConfigManager.getConfigOrDefault("timeInfo", false)){
            logger.info(ORANGE + "DEV-TIME: " + message + RESET);
        }
    }

    public static void web(String message) {
        if((boolean)ConfigManager.getConfigOrDefault("webInfo", false)){
            logger.info(ORANGE + "DEV-WEB: " + message + RESET);
        }
    }

    public static void debug(String message) {
        if (onDebug) {
            logger.info(ORANGE + "DEBUG: " + message + RESET);
        }
    }

    public static void debugSection(String title) {
        debug("=== " + title + " ===");
    }

    public static void action(String message) {
        info("[Action] " + message);
    }

    public static void debugPlayer(String context, OfflinePlayer player) {
        if (!onDebug || player == null) {
            return;
        }
        debug(context + " player=" + player.getName()
                + " uuid=" + player.getUniqueId()
                + " online=" + player.isOnline()
                + " playedBefore=" + player.hasPlayedBefore());
    }

    public static void debugFile(String context, File file) {
        if (!onDebug || file == null) {
            return;
        }
        debug(context + " path=" + file.getAbsolutePath()
                + " exists=" + file.exists()
                + " size=" + (file.exists() ? file.length() + "B" : "n/a"));
    }

    public static String summarizeItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "air";
        }
        return item.getType().name() + "x" + item.getAmount();
    }

    public static void debugItemStacks(String label, ItemStack[] items) {
        if (!onDebug || items == null) {
            return;
        }
        StringBuilder builder = new StringBuilder(label).append(" (").append(items.length).append(" slots): ");
        int itemCount = 0;
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack item = items[slot];
            if (item == null || item.getType().isAir()) {
                continue;
            }
            if (itemCount > 0) {
                builder.append(", ");
            }
            builder.append("[").append(slot).append("]=").append(summarizeItem(item));
            itemCount++;
        }
        if (itemCount == 0) {
            builder.append("empty");
        }
        debug(builder.toString());
    }

    public static void debugListTag(String label, ListTag listTag) {
        if (!onDebug) {
            return;
        }
        if (listTag == null) {
            debug(label + ": null list");
            return;
        }
        debug(label + ": " + listTag.size() + " entries");
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompoundOrEmpty(i);
            int slot = itemTag.getByte(NbtTags.getSlot()).map(Byte::intValue).orElse(-1);
            String id = itemTag.getString(NbtTags.getId()).orElse("unknown");
            int count = itemTag.getInt(NbtTags.getCount()).orElse(0);
            debug("  entry[" + i + "] slot=" + slot + " id=" + id + " count=" + count);
        }
    }

    public static void sendMessage(String message, String mode) {
        StringBuilder builder = new StringBuilder();
        String[] tokens = message.split(" /d ");
        for (String token : tokens) {
            builder.append(getColor(token));
        }
        switch (mode.toLowerCase()) {
            case "debug":
                debug(builder.toString());
                break;
            case "info":
                info(builder.toString());
                break;
            case "warn":
                warn(builder.toString());
                break;
            case "error":
                error(builder.toString());
                break;
            case "success":
                success(builder.toString());
                break;
        }
    }

    public static String getColor(String message) {
        return switch (message.toLowerCase()) {
            case "red" -> RED;
            case "blue" -> BLUE;
            case "green" -> GREEN;
            case "yellow" -> YELLOW;
            case "lightblue" -> LIGHTBLUE;
            case "orange" -> ORANGE;
            case "reset" -> RESET;
            default -> message;
        };
    }
}
