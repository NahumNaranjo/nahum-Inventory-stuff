package dev.nahum.nahumInventoryStuff;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigDatum {
    private static final Pattern DURATION_PATTERN =
            Pattern.compile("(?:(\\d+(?:\\.\\d+)?)\\s*d(?:ays?)?)?\\s*" +  // Days (optional)
                    "(?:(\\d+(?:\\.\\d+)?)\\s*h(?:ours?)?)?\\s*" +   // Hours (optional)
                    "(?:(\\d+(?:\\.\\d+)?)\\s*m(?:in(?:utes?)?)?)?"); // Minutes (optional)
    private String name;
    private String path;
    private ConfigDatum parent;
    private Object value;
    private Object defaultValue;

    ConfigDatum(String name) {
        this.name = name;
        this.path = null;
        this.value = null;
        this.parent = null;
        this.defaultValue = null;
    }

    ConfigDatum(String name, Object value,  Object defaultValue) {
        this.name = name;
        this.path = null;
        this.value = value;
        this.parent = null;
        this.defaultValue = defaultValue;
    }

    ConfigDatum(String name, ConfigDatum parent) {
        this.name = name;
        this.path = null;
        this.value = null;
        this.parent = parent;
        this.defaultValue = null;
    }

    ConfigDatum(String name, Object value, ConfigDatum parent) {
        this.name = name;
        this.path = null;
        this.value = value;
        this.parent = parent;
        this.defaultValue = null;
    }
    ConfigDatum(String name, Object value, Object defaultValue, ConfigDatum parent) {
        this.name = name;
        this.path = null;
        if(value != null) {
            this.value = value;
        }
        this.parent = parent;
        this.defaultValue = defaultValue;
    }

    ConfigDatum(String name, Object value, ConfigDatum parent, Object defaultValue) {
        this.name = name;
        this.path = null;
        this.value = value;
        this.parent = parent;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        if (this.path == null) {
            StringBuilder parts = new StringBuilder(this.name);
            ConfigDatum parent = this.parent;
            while (parent != null) {
                parts.insert(0, parent.name + ".");
                parent = parent.parent;
            }
            this.path = parts.toString();
            return parts.toString();
        } else return this.path;
    }

    public ConfigDatum getParent() {
        return parent;
    }

    public void setParent(ConfigDatum parent) {
        this.parent = parent;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public LocalTime getParsedSchedule(String value) {
        if ((Boolean) ConfigManager.getConfig("fixedMode") == false) {
            return null;
        }

        if (value == null) {
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
        if ((Boolean) ConfigManager.getConfig("fixedMode") == true) {
            return null;
        }

        if (value == null) {
            if (this.value == null) return null;
            value = this.value.toString();
        }

        if (value == null || value.isBlank()) {
            GoodLogger.warn("Lapse is empty and could not be parsed.");
            return null;
        }

        String cleaned = value.toLowerCase().trim().replaceAll("\\s+", "");

        // Handle simple number - treat as minutes
        if (cleaned.matches("\\d+")) {
            long minutes = Long.parseLong(cleaned);
            if (minutes < 1) {
                GoodLogger.warn("Duration must be at least 1 minute: " + value);
                return null;
            }
            return Duration.ofMinutes(minutes);
        }

        Matcher matcher = DURATION_PATTERN.matcher(cleaned);
        if (matcher.matches()) {
            double days = matcher.group(1) != null ? Double.parseDouble(matcher.group(1)) : 0.0;
            double hours = matcher.group(2) != null ? Double.parseDouble(matcher.group(2)) : 0.0;
            double minutes = matcher.group(3) != null ? Double.parseDouble(matcher.group(3)) : 0.0;

            if (days == 0 && hours == 0 && minutes == 0) {
                GoodLogger.warn("Duration must be greater than 0: " + value);
                return null;
            }

            double totalMinutes = days * 24 * 60 + hours * 60 + minutes;
            return Duration.ofMinutes(Math.round(totalMinutes));
        }

        GoodLogger.warn("Invalid duration format: " + value + " (expected: 30m, 2h, 1h30m, 1d12h30m, or just a number for minutes)");
        return null;
    }

    public LocalDate getMaxAge(String lapse) {
        if (lapse == null) {
            lapse = this.value.toString();
        }
        lapse = lapse.replaceAll("d|days|day", "");

        int days = Integer.parseInt(lapse);
        return LocalDate.now().minusDays(days);
    }
}
