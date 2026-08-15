package dev.nahum.nahumInventoryStuff;

import org.apache.commons.lang3.time.DurationUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;


public class ConfigManager {
    private static NahumInventoryStuff instance =  NahumInventoryStuff.getInstance();
    private static FileConfiguration config = instance.getConfig();
    private static ConfigDatum backup = new ConfigDatum("backupSettings");
    private static ConfigDatum autoBackup = new ConfigDatum("autoBackup", backup);
    private static ConfigDatum fixedMode = new ConfigDatum("fixedMode", backup);
    private static ConfigDatum lapse = new ConfigDatum("lapse",  backup);
    private static ConfigDatum schedule =  new ConfigDatum("schedule", backup);
    private static ConfigDatum debug = new ConfigDatum("debug");
    private static ConfigDatum onDebug = new ConfigDatum("onDebug", debug);

    private static ConfigDatum[] data = {
            autoBackup,fixedMode,lapse,schedule, // backup
            onDebug //debug
    };

    public static String getSchedule() { return schedule.getValue().toString(); }
    public static String getLapse() { return lapse.getValue().toString(); }
    public static Duration getParsedLapse() {return lapse.getParsedLapse(null);}
    public static LocalTime getParsedSchedule() {return schedule.getParsedSchedule(null);}
    public static boolean isFixedMode() { return (Boolean)fixedMode.getValue(); }
    public static boolean hasAutoBackup() { return (Boolean)autoBackup.getValue(); }

    public static void setAutoBackup(boolean autoBackup) { ConfigManager.autoBackup.setValue(autoBackup); }
    public static void setFixedMode(boolean fixedMode) { ConfigManager.fixedMode.setValue(fixedMode); }
    public static void setLapse(String lapse) { ConfigManager.lapse.setValue(lapse); }
    public static void setSchedule(String schedule) { ConfigManager.schedule.setValue(schedule); }
    public static void setOnDebug(boolean onDebug) { ConfigManager.onDebug.setValue(onDebug); }

    public static boolean checkLapseFormat(String lapse){
        return backup.getParsedLapse(lapse) != null;
    }

    public static boolean checkScheduleFormat(String schedule){
        return backup.getParsedSchedule(schedule) != null;
    }

    public static void setConfig(String name, Object value){
        for(ConfigDatum d : data){
            switch(d.getName()){
                case "autoBackup":
                    config.set(autoBackup.getPath(), value);
                    break;
                case "fixedMode":
                    config.set(fixedMode.getPath(), value);
                    break;
                case "debug":
                    config.set(debug.getPath(), value);
                    break;
                case "lapse":
                    if(checkLapseFormat(value.toString())){
                        config.set(lapse.getPath(), value);
                    } else {
                        GoodLogger.debug("Invalid format set for lapse. Skipping order...");
                    }
                    break;
                case "schedule":
                    if(checkScheduleFormat(value.toString())){
                        config.set(schedule.getPath(), value);
                    } else {
                        GoodLogger.debug("Invalid format set for schedule. Skipping order...");
                    }
                    break;
            }
        }
    }

    public static void load(){
        setAutoBackup(config.getBoolean(autoBackup.getPath()));
        setFixedMode(config.getBoolean(fixedMode.getPath()));
        setLapse(config.getString(lapse.getPath()));
        setSchedule(config.getString(schedule.getPath()));
        setOnDebug(config.getBoolean(onDebug.getPath()));
    }
    public static void save(){
        for(ConfigDatum d : data){
            config.set(d.getPath(), d.getValue());
        }
    }
    public static Object getConfig(String name){
        for(ConfigDatum d : data){
            if(name.equals(d.getName())){
                return d.getValue();
            }
        }
        return null;
    }
    public static ConfigDatum getConfigDatum(String name){
        for(ConfigDatum d : data){
            if(name.equals(d.getName())){
                return d;
            }
        }
        return null;
    }

    public static List<String> getAllPaths(){
        List<String> list = new LinkedList<>();
        for(ConfigDatum d : data){
            list.add(d.getPath());
        }
        return list;
    }
    public static Map<String, Object> getAllConfigs(){
        Map<String, Object> map = new LinkedHashMap<>();
        for(ConfigDatum d : data){
            map.put(d.getPath(), d.getValue().toString());
        }
        return map;
    }
}
