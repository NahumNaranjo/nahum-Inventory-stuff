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
    private final Object defaultValue;
    private Class<?> type = Object.class;

    public enum types {
        STRING,
        INTEGER,
        DOUBLE,
        BOOLEAN,
        DURATION,
        LOCALTIME,
        LOCALDATE,
        OBJECT
    }

    ConfigDatum(String name) {
        this.name = name;
        this.path = null;
        this.value = null;
        this.parent = null;
        this.defaultValue = null;
    }

    ConfigDatum(String name, Object value, Object defaultValue, types type) {
        this.name = name;
        this.path = null;
        this.value = value;
        this.parent = null;
        this.defaultValue = defaultValue;
        this.type = getType(type);
    }

    ConfigDatum(String name, ConfigDatum parent) {
        this.name = name;
        this.path = null;
        this.value = null;
        this.parent = parent;
        this.defaultValue = null;
        this.type = null;
    }

    ConfigDatum(String name, Object value, ConfigDatum parent, types type) {
        this.name = name;
        this.path = null;
        this.value = value;
        this.parent = parent;
        this.defaultValue = null;
        this.type = getType(type);
    }

    ConfigDatum(String name, ConfigDatum parent, types type) {
        this.name = name;
        this.path = null;
        this.value = null;
        this.parent = parent;
        this.defaultValue = null;
        this.type = getType(type);
    }

    ConfigDatum(String name, Object value, Object defaultValue, ConfigDatum parent, types type) {
        this.name = name;
        this.path = null;
        if (value != null) {
            this.value = value;
        }
        this.parent = parent;
        this.defaultValue = defaultValue;
        this.type = getType(type);
    }

    public Class<?> getType(types type) {
        return switch (type) {
            case types.STRING -> String.class;
            case types.INTEGER -> Integer.class;
            case types.DOUBLE -> Double.class;
            case types.BOOLEAN -> Boolean.class;
            case types.DURATION -> Duration.class;
            case types.LOCALTIME -> LocalTime.class;
            case types.LOCALDATE -> LocalDate.class;
            default -> Object.class;
        };
    }

    public Class<?> getDataType() {
        return type;
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

    public Object getValue() {return this.value;}

    public <T> T getTypedValue() {
        if(this.type.isInstance(this.value)) {
            return (T) this.value;
        } else {
            return null;
        }
    }

    public boolean isCompatible(Object value){
        if(this.type.isInstance(value)) {
            return true;
        }
        if(value instanceof String s) {
            if(this.value == Integer.class){
                try{
                    Integer.parseInt(s);
                } catch(NumberFormatException e){
                    GoodLogger.error("Invalid number format, error: \n" + e.getMessage());
                    return false;
                }
                return true;
            }
            if(this.value == Double.class){
                try{
                    Double.parseDouble(s);
                } catch(NumberFormatException e){
                    GoodLogger.error("Invalid number format, error: \n" + e.getMessage());
                    return false;
                }
                return true;
            }
            if (this.type == Duration.class) {
                // Check if it's a valid duration format
                return ConfigManager.checkLapseFormat(s);
            }

            if (this.type == LocalTime.class) {
                // Check if it's a valid time format
                return ConfigManager.checkScheduleFormat(s);
            }

            if (this.type == LocalDate.class) {
                try {
                    LocalDate.parse(s);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            if (this.type == String.class) {
                return true;
            }
        }

        if (value instanceof Number) {
            if (this.type == Integer.class || this.type == Double.class ||
                    this.type == Long.class || this.type == Float.class) {
                return true;
            }
        }

        if (value instanceof Number && this.type == Boolean.class) {
            int num = ((Number) value).intValue();
            return num == 0 || num == 1;
        }

        return false;
    }

    public <T> T getTypedValueOrDefault(T defaultValue) {
        if(this.type.isInstance(this.value)) {
            return (T) this.value;
        } else {
            return defaultValue;
        }
    }

    public void setValue(Object value) {
        if(value instanceof String s && isCompatible(value)) {
            if(this.value == String.class){
                this.value = s;
                return;
            }
            if(this.value == Integer.class){
                this.value = Integer.parseInt(s);
            }
            if(this.value == Double.class){
                this.value = Double.parseDouble(s);
            }
            if(this.type == Duration.class) {
                this.value = getParsedLapse(s);
            }
            if(this.type == LocalTime.class) {
                this.value = LocalTime.parse(s);
            }
            if(this.type == LocalDate.class) {
                this.value = LocalDate.parse(s);
            }
            if(this.type == Boolean.class){
                this.value = Boolean.parseBoolean(s);
            }
            return;
        }
        if(this.type.isInstance(value)) {
            this.value = value;
        } else {
            GoodLogger.error("Caught error: ", new IllegalArgumentException("Value must be of type " + this.type));
        }
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public LocalTime getParsedSchedule(String value) {
        if (ConfigManager.getConfigOrDefault("fixedMode", false) == false) {
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
        if ((Boolean) ConfigManager.getConfigOrDefault("fixedMode", false) == true) {
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
