package dev.nahum.nahumInventoryStuff;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {
    public static String getValue(String src, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\"[^\"]*\"|\\d+)");
        Matcher matcher = pattern.matcher(src);

        if (matcher.find()) {
            String value = matcher.group(1);
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }
        return null;
    }
}
