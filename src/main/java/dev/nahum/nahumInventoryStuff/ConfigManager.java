package dev.nahum.nahumInventoryStuff;

import org.bukkit.configuration.file.FileConfiguration;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ConfigManager {
    private static NahumInventoryStuff instance = NahumInventoryStuff.getInstance();
    private static FileConfiguration config = instance.getConfig();
    private static ConfigDatum backup = new ConfigDatum("backupSettings");
    private static ConfigDatum autoBackup = new ConfigDatum("autoBackup", backup);
    private static ConfigDatum fixedMode = new ConfigDatum("fixedMode", backup);
    private static ConfigDatum lapse = new ConfigDatum("lapse", backup);
    private static ConfigDatum schedule = new ConfigDatum("schedule", backup);
    private static ConfigDatum deleteOlderThan = new ConfigDatum("deleteOlderThan",  backup);
    private static ConfigDatum debug = new ConfigDatum("debug");
    private static ConfigDatum onDebug = new ConfigDatum("onDebug", debug);

    private static ConfigDatum[] data = {
            autoBackup, fixedMode, lapse, schedule, deleteOlderThan,
            onDebug
    };

    public static String getSchedule() {
        return schedule.getValue() != null ? schedule.getValue().toString() : null;
    }

    public static String getLapse() {
        return lapse.getValue() != null ? lapse.getValue().toString() : null;
    }
    public static LocalDate getMaxAge(String lapse) {
        return deleteOlderThan.getMaxAge(lapse);
    }

    public static boolean checkMaxAge(String lapse) {
        try{
            deleteOlderThan.getMaxAge(lapse);
        } catch(Exception e){
            return false;
        }
        return true;
    }

    public static Duration getParsedLapse() {
        return lapse.getParsedLapse(null);
    }

    public static LocalTime getParsedSchedule() {
        return schedule.getParsedSchedule(null);
    }

    public static boolean isFixedMode() {
        return fixedMode.getValue() != null ? (Boolean)fixedMode.getValue() : false;
    }

    public static boolean hasAutoBackup() {
        return autoBackup.getValue() != null && (Boolean) autoBackup.getValue();
    }

    public static void setAutoBackup(boolean autoBackup) {
        ConfigManager.autoBackup.setValue(autoBackup);
    }

    public static void setMaxAge(String lapse) {
        deleteOlderThan.setValue(lapse);
    }

    public static void setFixedMode(boolean fixedMode) {
        ConfigManager.fixedMode.setValue(fixedMode);
    }

    public static void setLapse(String lapse) {
        ConfigManager.lapse.setValue(lapse);
    }

    public static void setSchedule(String schedule) {
        ConfigManager.schedule.setValue(schedule);
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

    public static void setConfig(String name, Object value) {
        for (ConfigDatum d : data) {
            if (d.getName().equals(name)) {
                d.setValue(value);

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
                        if(checkLapseFormat(value.toString())) {
                            config.set(deleteOlderThan.getPath(), value);
                        } else {
                            GoodLogger.warn("Invalid format for deleteOlderThan: " + value);
                            return;
                        }
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
        setAutoBackup(config.getBoolean(autoBackup.getPath(), false));
        setFixedMode(config.getBoolean(fixedMode.getPath(), false));
        setLapse(config.getString(lapse.getPath(), "30m"));
        setSchedule(config.getString(schedule.getPath(), "03:00"));
        setMaxAge(config.getString(deleteOlderThan.getPath(), "7d"));

        setOnDebug(config.getBoolean(onDebug.getPath(), false));
    }

    public static void reload() {
        instance.reloadConfig();
        config = instance.getConfig();
        load();
        NahumInventoryStuff.setOnDebug((Boolean) getConfig("onDebug"));
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
}