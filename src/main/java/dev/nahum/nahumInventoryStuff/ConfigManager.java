package dev.nahum.nahumInventoryStuff;

import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ConfigManager {
    private static final NahumInventoryStuff instance = NahumInventoryStuff.getInstance();
    private static FileConfiguration config = instance.getConfig();

    private static final ConfigDatum backup = new ConfigDatum("backupSettings");
    private static final ConfigDatum autoBackup = new ConfigDatum("autoBackup", backup);
    private static final ConfigDatum autoDelete = new ConfigDatum("autoDelete", backup);
    private static final ConfigDatum fixedMode = new ConfigDatum("fixedMode", backup);
    private static final ConfigDatum lapse = new ConfigDatum("lapse", backup);
    private static final ConfigDatum schedule = new ConfigDatum("schedule", backup);
    private static final ConfigDatum deleteOlderThan = new ConfigDatum("deleteOlderThan", backup);

    private static final ConfigDatum snapshotSettings = new ConfigDatum("snapshotSettings", backup);

    private static final ConfigDatum playerSnapshots = new ConfigDatum("playerSnapshots", snapshotSettings);
    private static final ConfigDatum maxDeathSnapshots = new ConfigDatum("maxDeathSnapshots", playerSnapshots);
    private static final ConfigDatum maxJoinSnapshots = new ConfigDatum("maxJoinSnapshots", playerSnapshots);
    private static final ConfigDatum maxLeaveSnapshots = new ConfigDatum("maxLeaveSnapshots", playerSnapshots);
    private static final ConfigDatum maxForcedSnapshots = new ConfigDatum("maxForcedSnapshots", playerSnapshots);

    private static final ConfigDatum adminSnapshots = new ConfigDatum("adminSnapshots", snapshotSettings);
    private static final ConfigDatum maxPlayers = new ConfigDatum("maxPlayers", adminSnapshots);
    private static final ConfigDatum maxSnapshots = new ConfigDatum("maxSnapshots", adminSnapshots);
    private static final ConfigDatum debug = new ConfigDatum("debug");
    private static final ConfigDatum onDebug = new ConfigDatum("onDebug", debug);

    private static final ConfigDatum[] data = {
            autoBackup, autoDelete, fixedMode, lapse, schedule, deleteOlderThan,
            maxDeathSnapshots, maxJoinSnapshots, maxLeaveSnapshots, maxForcedSnapshots,
            maxPlayers, maxSnapshots,
            onDebug
    };

    public static String getSchedule() {
        return schedule.getValue() != null ? schedule.getValue().toString() : null;
    }

    public static void setSchedule(String schedule) {
        ConfigManager.schedule.setValue(schedule);
    }

    public static String getLapse() {
        return lapse.getValue() != null ? lapse.getValue().toString() : null;
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

    public static int getMaxDeathSnapshots() {
        return maxDeathSnapshots.getValue() != null ? (Integer) maxDeathSnapshots.getValue() : 10;
    }

    public static void setMaxDeathSnapshots(int newMax) {
        ConfigManager.maxDeathSnapshots.setValue(String.valueOf(newMax));
    }

    public static int getMaxJoinSnapshots() {
        return maxJoinSnapshots.getValue() != null ? (Integer) maxJoinSnapshots.getValue() : 10;
    }

    public static void setMaxJoinSnapshots(int newMax) {
        ConfigManager.maxJoinSnapshots.setValue(String.valueOf(newMax));
    }

    public static int getMaxLeaveSnapshots() {
        return maxLeaveSnapshots.getValue() != null ? (Integer) maxLeaveSnapshots.getValue() : 10;
    }

    public static void setMaxLeaveSnapshots(int newMax) {
        ConfigManager.maxLeaveSnapshots.setValue(String.valueOf(newMax));
    }

    public static int getMaxForcedSnapshots() {
        return maxForcedSnapshots.getValue() != null ? (Integer) maxForcedSnapshots.getValue() : 10;
    }

    public static void setMaxForcedSnapshots(int newMax) {
        ConfigManager.maxForcedSnapshots.setValue(String.valueOf(newMax));
    }

    public static int getMaxPlayers() {
        return maxPlayers.getValue() != null ? (Integer) maxPlayers.getValue() : 10;
    }

    public static void setMaxPlayers(int newMax) {
        ConfigManager.maxPlayers.setValue(String.valueOf(newMax));
    }

    public static int getMaxSnapshots() {
        return maxSnapshots.getValue() != null ? (Integer) maxSnapshots.getValue() : 10;
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
                int max = 0;

                switch (name) {
                    case "autoBackup":
                        config.set(autoBackup.getPath(), value);
                        break;
                    case "fixedMode":
                        config.set(fixedMode.getPath(), value);
                        break;
                    case "onDebug":
                        config.set(onDebug.getPath(), value);
                        break;
                    case "lapse":
                        if (checkLapseFormat(value.toString())) {
                            config.set(lapse.getPath(), value);
                        } else {
                            GoodLogger.warn("Invalid format for lapse: " + value);
                            return;
                        }
                        break;
                    case "schedule":
                        if (checkScheduleFormat(value.toString())) {
                            config.set(schedule.getPath(), value);
                        } else {
                            GoodLogger.warn("Invalid format for schedule: " + value);
                            return;
                        }
                        break;
                    case "deleteOlderThan":
                        if (checkLapseFormat(value.toString())) {
                            config.set(deleteOlderThan.getPath(), value);
                        } else {
                            GoodLogger.warn("Invalid format for deleteOlderThan: " + value);
                            return;
                        }
                        break;
                    case "maxDeathSnapshots":
                        try {
                            max = Integer.parseInt(value.toString());
                        } catch (Exception e) {
                            GoodLogger.warn("Invalid format for maxDeathSnapshots: " + value);
                            return;
                        }
                        setMaxDeathSnapshots(max);
                        break;
                    case "maxJoinSnapshots":
                        try {
                            max = Integer.parseInt(value.toString());
                        } catch (Exception e) {
                            GoodLogger.warn("Invalid format for maxJoinSnapshots: " + value);
                            return;
                        }
                        setMaxJoinSnapshots(max);
                        break;
                    case "maxLeaveSnapshots":
                        try {
                            max = Integer.parseInt(value.toString());
                        } catch (Exception e) {
                            GoodLogger.warn("Invalid format for maxLeaveSnapshots: " + value);
                            return;
                        }
                        setMaxLeaveSnapshots(max);
                        break;
                    case "maxForcedSnapshots":
                        try {
                            max = Integer.parseInt(value.toString());
                        } catch (Exception e) {
                            GoodLogger.warn("Invalid format for maxForcedSnapshots: " + value);
                            return;
                        }
                        setMaxForcedSnapshots(max);
                        break;
                    case "maxPlayers":
                        try {
                            max = Integer.parseInt(value.toString());
                        } catch (Exception e) {
                            GoodLogger.warn("Invalid format for maxPlayers: " + value);
                            return;
                        }
                        setMaxPlayers(max);
                        break;
                    case "maxSnapshots":
                        try {
                            max = Integer.parseInt(value.toString());
                        } catch (Exception e) {
                            GoodLogger.warn("Invalid format for maxSnapshots: " + value);
                            return;
                        }
                        setMaxSnapshots(max);
                        break;
                    default:
                        GoodLogger.warn("Unknown config key: " + name);
                        return;
                }
                // Save after setting
                instance.saveConfig();
                break;
            }
        }
    }

    public static void load() {
        //<editor-fold desc="Backup">
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
        //</editor-fold>
        //<editor-fold desc="Debug">
        setOnDebug(config.getBoolean(onDebug.getPath(), false));
        //</editor-fold>
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

    public static Object getConfig(String name) {
        for (ConfigDatum d : data) {
            if (name.equals(d.getName())) {
                return d.getValue();
            }
        }
        return null;
    }

    public static Object getConfigOrDefault(String name, Object def) {
        Object val = getConfig(name);
        if (val == null &&  def == null) {
            ConfigDatum datum = getConfigDatum(name);
            if(datum == null){
                throw  new RuntimeException(
                        "Error getting " + name +
                                ". Check your config name and please, place a " +
                                "default value when using ConfigManager.getConfigOrDefault()."
                );
            }
            return datum.getDefaultValue();
        }

        if(val == null || def != null) {
            return def;
        }

        return val;
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