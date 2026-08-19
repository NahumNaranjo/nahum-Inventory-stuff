package dev.nahum.nahumInventoryStuff;

import org.bukkit.plugin.PluginDescriptionFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class UpdateChecker {
    private static final String availableLinks = """
            \
            https://www.curseforge.com/minecraft/bukkit-plugins/nahuminventorystuff
            https://modrinth.com/plugin/nahuminventorystuff
            https://www.spigotmc.org/resources/nahuminventorystuff.137696/
            https://hangar.papermc.io/NahumNaranjo/NahumInventoryStuff""";
    private static String latestVersion = "1";
    private static String latestDate = "1/1/2001";
    private static String currentVersion = "1";

    public static String getLatestVersion() {
        return latestVersion;
    }

    private static void setLatestVersion(String newVersion) {
        latestVersion = newVersion;
    }

    public static String getLatestDate() {
        return latestDate;
    }

    private static void setLatestDate(String newDate) {
        latestDate = newDate;
    }

    public static String getAvailableLinks() {
        return availableLinks;
    }

    public static String getCurrentVersion() {
        return currentVersion;
    }

    private static void setCurrentVersion(String newVersion) {
        currentVersion = newVersion;
    }

    public static void setUp() {
        fetchSpigotData();
        PluginDescriptionFile desc = NahumInventoryStuff.getInstance().getDescription();
        setCurrentVersion(desc.getVersion());
    }

    public static void fetchSpigotData() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.spiget.org/v2/resources/137696/updates/latest")).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            GoodLogger.debug("Spigot data found: \n" + responseBody);
            setLatestVersion(JsonParser.getValue(responseBody, "title"));
            long date = Long.parseLong(JsonParser.getValue(responseBody, "date") != null ? JsonParser.getValue(responseBody, "date") : "-1");
            GoodLogger.debug("Spigot date found: " + date);
            setLatestDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date(date * 1000)));
        } catch (Exception e) {
            GoodLogger.debug("Failed to get current version, please check your internet connection." + "\n" + e.getMessage());
        }
    }
}
