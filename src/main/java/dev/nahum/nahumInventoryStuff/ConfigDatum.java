package dev.nahum.nahumInventoryStuff;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigDatum {
    private String name;
    private String path;
    private ConfigDatum parent;
    private Object value;
    private static final Pattern DURATION_PATTERN =
            Pattern.compile("(?:(\\d+)d)?\\s*(?:(\\d+)h)?\\s*(?:(\\d+)m)?");

    public String getName() {return name;}
    public String getPath() {
        if(this.path == null) {
            StringBuilder parts = new StringBuilder(this.name);
            ConfigDatum parent = this.parent;
            while(parent != null){
                parts.insert(0, parent.name + ".");
                parent = parent.parent;
            }
            this.path = parts.toString();
            return parts.toString();
        } else return this.path;
    }
    public ConfigDatum getParent() {return parent;}
    public Object getValue() {return value;}

    public void setName(String name) {this.name = name;}
    public void setParent(ConfigDatum parent) {this.parent = parent;}
    public void setValue(Object value) {this.value = value;}

    ConfigDatum(String name){
        this.name = name;
        this.path = null;
        this.value = null;
        this.parent = null;
    }

    ConfigDatum(String name, Object value) {
        this.name = name;
        this.path = null;
        this.value = value;
        this.parent = null;
    }

    ConfigDatum(String name, ConfigDatum parent) {
        this.name = name;
        this.path = null;
        this.value = null;
        this.parent = parent;
    }

    ConfigDatum(String name, Object value, ConfigDatum parent) {
        this.name = name;
        this.path = null;
        this.value = value;
        this.parent = parent;
    }

    public LocalTime getParsedSchedule(String value){
        if((Boolean)ConfigManager.getConfig("fixedMode") == false){
            return null;
        }

        if(value == null){
            value = this.value.toString();
        }

        if (value == null || value.isBlank()) {
            GoodLogger.warn("Schedule is empty and could not be parsed.");
        }

        String cleaned = value.toLowerCase()
                .replaceAll("hrs|hr|h", "") // Strip hour labels
                .replaceAll("\\s+", "");     // Strip all whitespaces

        if (cleaned.matches("\\d+")) {
            int hour = Integer.parseInt(cleaned);
            if (hour < 0 || hour > 23) {
                throw new IllegalArgumentException("Hour must be between 0 and 23: " + value);
            }
            return LocalTime.of(hour, 0);
        }

        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts[0].length() == 1) {
                cleaned = "0" + cleaned;
            }

            return LocalTime.parse(cleaned, DateTimeFormatter.ofPattern("HH:mm"));
        }

        GoodLogger.warn("Invalid schedule format: " + value);
        return null;
    }
    public Duration getParsedLapse(String value) {
        if((Boolean)ConfigManager.getConfig("fixedMode") == true){
            return null;
        }

        if(value == null){
            value = this.value.toString();
        }

        Matcher matcher = DURATION_PATTERN.matcher(value.trim());

        if (matcher.matches()) {
            double days = matcher.group(1) != null ? Double.parseDouble(matcher.group(1)) : 0.0;
            double hours = matcher.group(2) != null ? Double.parseDouble(matcher.group(2)) : 0.0;
            double minutes = matcher.group(3) != null ? Double.parseDouble(matcher.group(3)) : 0.0;

            double totalSeconds = 0;
            totalSeconds += days * 24 * 60 * 60; // 1 day = 86,400 seconds
            totalSeconds += hours * 60 * 60;     // 1 hour = 3,600 seconds
            totalSeconds += minutes * 60;        // 1 minute = 60 seconds

            return Duration.ofSeconds(Math.round(totalSeconds));
        }

        GoodLogger.warn("Got " + "Invalid duration format: " + value);
        return null;
    }
}
