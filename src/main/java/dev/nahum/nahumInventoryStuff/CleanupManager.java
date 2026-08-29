package dev.nahum.nahumInventoryStuff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

public class CleanupManager {
    private CleanupManager() {
    } // no instantiation for you either! >:(

    public static void deleteOldSnapshots(File folder, String name) {
        int max = (int)ConfigManager.getConfigOrDefault(name, 10);
        List<File> files = FileManager.getAllRegularFiles(folder);


        if (max == 0 || files.size() <= max) return;

        files.sort(Comparator.comparing(f -> FileManager.getLocalDateTimeFromFileName(f.getName())));

        int toDelete = files.size() - max;
        int deleted = 0;

        for (int i = 0; i <= toDelete; i++) {
            File file = files.get(i);

            if (file != null && file.delete()) {
                GoodLogger.debug("Deleted old snapshot: " + file.getAbsolutePath());
                deleted++;
            } else if (file != null) {
                GoodLogger.debug("Failed to delete: " + file.getAbsolutePath());
            }

            files.remove(file);
        }
        GoodLogger.debug("finished deleting snapshots with max of " + max + " and size of " + files.size() + " deleted " + deleted);
    }

    public static void deleteOldPlayers(File folder, String name) {
        int max = (int)ConfigManager.getConfigOrDefault(name, 10);

        if (max == 0) return; // Keep all

        List<File> playerDirs = FileManager.getAllDirectories(folder);
        if (playerDirs.size() <= max) return;
        playerDirs.sort(Comparator.comparing(FileManager::getLocalDateTimeFromAtt));
        int toDelete = playerDirs.size() - max;
        int deleted = 0;

        for (File file : playerDirs) {
            if (deleted >= toDelete) break;

            if (FileManager.deleteFolder(file)) {
                GoodLogger.debug("Deleted old player data: " + file.getAbsolutePath());
                deleted++;
            } else {
                GoodLogger.warn("Failed to delete: " + file.getAbsolutePath());
            }
        }
    }

    public static void deleteOldBackups(File folder) {
        LocalDate maxAge = ConfigManager.getMaxAge(null);
        try (Stream<Path> stream = Files.list(folder.toPath())) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

                            LocalDate age = attr.creationTime().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();

                            if (age.isBefore(maxAge)) {
                                GoodLogger.debug("\nDeleting old backup: " + path.toAbsolutePath() + "\nBecause is older than: " + maxAge.toString() + " with an age of " + age.toString());
                                Files.deleteIfExists(path);
                            }
                            if (path.toFile().exists()) {
                                LocalDate ageFromName = FileManager.getLocalDateFromFileName(path.toFile().getName().replace(".nahumbackup", ""));
                                if (ageFromName.isBefore(maxAge)) {
                                    GoodLogger.debug("\nDeleting old backup: " + path.toAbsolutePath() + "\nBecause is older than: " + maxAge.toString() + " with an age of " + ageFromName.toString());
                                    Files.deleteIfExists(path);
                                }
                            }
                            GoodLogger.debug("\nNot deleting old backup: " + path.toAbsolutePath() + "\nBecause is younger than: " + maxAge.toString() + " with an age of " + age.toString());
                        } catch (IOException e) {
                            System.err.println("Could not read attributes for: " + path.getFileName());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
        }
    }
}
