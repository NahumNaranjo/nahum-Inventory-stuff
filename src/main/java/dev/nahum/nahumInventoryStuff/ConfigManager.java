package dev.nahum.nahumInventoryStuff;

import ca.spottedleaf.dataconverter.converters.datatypes.DataType;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ConfigManager {
    private static final NahumInventoryStuff instance = NahumInventoryStuff.getInstance();
    private static FileConfiguration config = instance.getConfig();

    private static final ConfigDatum backup = new ConfigDatum("backupSettings");
    private static final ConfigDatum autoBackup = new ConfigDatum("autoBackup", backup, ConfigDatum.types.BOOLEAN);
    private static final ConfigDatum autoDelete = new ConfigDatum("autoDelete", backup, ConfigDatum.types.BOOLEAN);
    private static final ConfigDatum fixedMode = new ConfigDatum("fixedMode", backup, ConfigDatum.types.BOOLEAN);
    private static final ConfigDatum lapse = new ConfigDatum("lapse", backup, ConfigDatum.types.DURATION);
    private static final ConfigDatum schedule = new ConfigDatum("schedule", backup, ConfigDatum.types.LOCALTIME);
    private static final ConfigDatum deleteOlderThan = new ConfigDatum("deleteOlderThan", backup, ConfigDatum.types.LOCALDATE);

    private static final ConfigDatum snapshotSettings = new ConfigDatum("snapshotSettings", backup);

    private static final ConfigDatum playerSnapshots = new ConfigDatum("playerSnapshots", snapshotSettings);
    private static final ConfigDatum maxDeathSnapshots = new ConfigDatum("maxDeathSnapshots", playerSnapshots, ConfigDatum.types.INTEGER);
    private static final ConfigDatum maxJoinSnapshots = new ConfigDatum("maxJoinSnapshots", playerSnapshots, ConfigDatum.types.INTEGER);
    private static final ConfigDatum maxLeaveSnapshots = new ConfigDatum("maxLeaveSnapshots", playerSnapshots, ConfigDatum.types.INTEGER);
    private static final ConfigDatum maxForcedSnapshots = new ConfigDatum("maxForcedSnapshots", playerSnapshots, ConfigDatum.types.INTEGER);

    private static final ConfigDatum adminSnapshots = new ConfigDatum("adminSnapshots", snapshotSettings);
    private static final ConfigDatum maxPlayers = new ConfigDatum("maxPlayers", adminSnapshots);
    private static final ConfigDatum maxSnapshots = new ConfigDatum("maxSnapshots", adminSnapshots);

    private static final ConfigDatum debug = new ConfigDatum("debug");
    private static final ConfigDatum onDebug = new ConfigDatum("onDebug", debug, ConfigDatum.types.BOOLEAN);

    private static final ConfigDatum dev = new ConfigDatum("dev");
    private static final ConfigDatum onDev = new ConfigDatum("onDev", dev, ConfigDatum.types.BOOLEAN);
    private static final ConfigDatum timeInfo = new ConfigDatum("timeInfo", dev, ConfigDatum.types.BOOLEAN);
    private static final ConfigDatum webInfo = new ConfigDatum("webInfo", dev, ConfigDatum.types.BOOLEAN);

    private static final ConfigDatum[] data = {
            autoBackup, autoDelete, fixedMode, lapse, schedule, deleteOlderThan,
            maxDeathSnapshots, maxJoinSnapshots, maxLeaveSnapshots, maxForcedSnapshots,
            maxPlayers, maxSnapshots,

            onDebug,

            onDev, timeInfo, webInfo
    };

    public static void setSchedule(String schedule) {
        ConfigManager.schedule.setValue(schedule);
    }

    public static void setLapse(String lapse) {
        ConfigManager.lapse.setValue(lapse);
    }

    public static LocalDate getMaxAge(String lapse) {
        return deleteOlderThan.getMaxAge(lapse);
    }

    public static Duration getParsedLapse() {
        return lapse.getParsedLapse(null);
    }

    public static LocalTime getParsedSchedule() {
        return schedule.getParsedSchedule(null);
    }

    public static boolean isFixedMode() {
        return fixedMode.getValue() != null ? (Boolean) fixedMode.getValue() : false;
    }

    public static void setFixedMode(boolean fixedMode) {
        ConfigManager.fixedMode.setValue(fixedMode);
    }

    public static boolean hasAutoBackup() {
        return autoBackup.getValue() != null && (Boolean) autoBackup.getValue();
    }

    public static boolean hasAutoDelete() {
        return (boolean) autoDelete.getValue();
    }

    public static void setMaxDeathSnapshots(int newMax) {
        ConfigManager.maxDeathSnapshots.setValue(String.valueOf(newMax));
    }

    public static void setMaxJoinSnapshots(int newMax) {
        ConfigManager.maxJoinSnapshots.setValue(String.valueOf(newMax));
    }

    public static void setMaxLeaveSnapshots(int newMax) {
        ConfigManager.maxLeaveSnapshots.setValue(String.valueOf(newMax));
    }

    public static void setMaxForcedSnapshots(int newMax) {
        ConfigManager.maxForcedSnapshots.setValue(String.valueOf(newMax));
    }

    public static void setMaxPlayers(int newMax) {
        ConfigManager.maxPlayers.setValue(String.valueOf(newMax));
    }

    public static void setMaxSnapshots(int newMax) {
        ConfigManager.maxSnapshots.setValue(String.valueOf(newMax));
    }

    public static void setAutoBackup(boolean autoBackup) {
        ConfigManager.autoBackup.setValue(autoBackup);
    }

    public static void setAutoDelete(boolean autoDelete) {
        ConfigManager.autoDelete.setValue(autoDelete);
    }

    public static void setMaxAge(String lapse) {
        deleteOlderThan.setValue(lapse);
    }

    public static void setOnDebug(boolean onDebug) {
        ConfigManager.onDebug.setValue(onDebug);
    }

    public static void setOnDev(boolean onDev) {ConfigManager.onDev.setValue(onDev);}
    public static void setTimeInfo(boolean timeInfo) {ConfigManager.timeInfo.setValue(timeInfo);}
    public static void setWebInfo(boolean webInfo) {ConfigManager.webInfo.setValue(webInfo);}

    public static boolean checkLapseFormat(String lapse) {
        return backup.getParsedLapse(lapse) != null;
    }

    public static boolean checkScheduleFormat(String schedule) {
        return backup.getParsedSchedule(schedule) != null;
    }

    public static boolean checkMaxAge(String lapse) {
        try {
            deleteOlderThan.getMaxAge(lapse);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void setConfig(String name, Object value) {
        for (ConfigDatum d : data) {
            if (d.getName().equals(name)) {
                d.setValue(value);
                int num = 0;

                switch (name) {
                    case "autoBackup" -> config.set("autoBackup", value);
                    case "fixedMode" -> config.set(fixedMode.getPath(), value);
                    case "onDebug" -> config.set(onDebug.getPath(), value);
                    case "lapse" -> config.set(lapse.getPath(), value);
                    case "schedule" -> config.set(schedule.getPath(), value);
                    case "deleteOlderThan" -> config.set(deleteOlderThan.getPath(), value);
                    case "maxDeathSnapshots" -> config.set(maxDeathSnapshots.getPath(), value);
                    case "maxJoinSnapshots"-> config.set(maxJoinSnapshots.getPath(), value);
                    case "maxLeaveSnapshots"-> config.set(maxLeaveSnapshots.getPath(), value);
                    case "maxForcedSnapshots"-> config.set(maxForcedSnapshots.getPath(), value);
                    case "maxPlayers"-> config.set(maxPlayers.getPath(), value);
                    case "maxSnapshots"-> config.set(maxSnapshots.getPath(), value);
                    default-> GoodLogger.warn("Unknown config key: " + name);
                }
                // Save after setting
                instance.saveConfig();
                break;
            }
        }
    }

    public static void load() {
        setAutoBackup(config.getBoolean(autoBackup.getPath(), false));
        setFixedMode(config.getBoolean(fixedMode.getPath(), false));
        setLapse(config.getString(lapse.getPath(), "30m"));
        setSchedule(config.getString(schedule.getPath(), "03:00"));
        setMaxAge(config.getString(deleteOlderThan.getPath(), "7d"));
        setAutoDelete(config.getBoolean(autoDelete.getPath(), false));

        setMaxDeathSnapshots(config.getInt(maxDeathSnapshots.getPath(), 10));
        setMaxJoinSnapshots(config.getInt(maxJoinSnapshots.getPath(), 5));
        setMaxLeaveSnapshots(config.getInt(maxLeaveSnapshots.getPath(), 5));
        setMaxForcedSnapshots(config.getInt(maxForcedSnapshots.getPath(), 0));

        setMaxPlayers(config.getInt(maxPlayers.getPath(), 10));
        setMaxSnapshots(config.getInt(maxSnapshots.getPath(), 10));
        setOnDebug(config.getBoolean(onDebug.getPath(), false));

        setOnDev(config.getBoolean(onDev.getPath(), false));
        setWebInfo(config.getBoolean(webInfo.getPath(), false));
        setTimeInfo(config.getBoolean(timeInfo.getPath(), false));
    }

    public static void reload() {
        instance.reloadConfig();
        config = instance.getConfig();
        load();
        NahumInventoryStuff.setOnDebug((Boolean) getConfigOrDefault("onDebug", false));
    }

    public static void save() {
        for (ConfigDatum d : data) {
            config.set(d.getPath(), d.getValue());
        }
        instance.saveConfig();
        instance.reloadConfig();
    }

    public static <T> T getConfig(String name) {
        for (ConfigDatum d : data) {
            if (name.equals(d.getName())) {
                return d.getTypedValue();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getConfigOrDefault(String name, T defaultValue) {
        ConfigDatum datum = getConfigDatum(name);
        if (datum == null) {
            return defaultValue;
        }

        Object value = datum.getValue();
        if (value == null) {
            return defaultValue;
        }

        // Type safety check - ensure the value matches the expected type
        if (defaultValue != null && !defaultValue.getClass().isInstance(value)) {
            GoodLogger.warn("Type mismatch for " + name +
                    ". Expected: " + defaultValue.getClass().getSimpleName() +
                    ", got: " + value.getClass().getSimpleName());
            return defaultValue;
        }

        return (T) value;
    }

    public static ConfigDatum getConfigDatum(String name) {
        for (ConfigDatum d : data) {
            if (name.equals(d.getName())) {
                return d;
            }
        }
        return null;
    }

    public static List<String> getAllPaths() {
        List<String> list = new LinkedList<>();
        for (ConfigDatum d : data) {
            list.add(d.getPath());
        }
        return list;
    }

    public static Map<String, Object> getAllConfigs() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (ConfigDatum d : data) {
            map.put(d.getPath(), d.getValue() != null ? d.getValue().toString() : "null");
        }
        return map;
    }

    public static String getType() {
        return "null";
    }
}