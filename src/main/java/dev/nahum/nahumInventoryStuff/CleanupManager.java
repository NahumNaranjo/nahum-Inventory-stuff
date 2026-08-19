package dev.nahum.nahumInventoryStuff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
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
        int max;
        try {
            max = Integer.parseInt((String) ConfigManager.getConfig(name));
        } catch (Exception exception) {
            GoodLogger.warn("Failed to delete old snapshots for " + name + ": \n" + exception.getMessage());
            return;
        }

        if (max == 0) return; // Keep all

        List<File> playerDirs = new ArrayList<>();
        Map<FileTime, File> timeToFile = new HashMap<>();
        List<FileTime> creationTimes = new ArrayList<>();

        // Read all player directories and their creation times
        try (Stream<Path> stream = Files.list(folder.toPath())) {
            stream.filter(Files::isDirectory)
                    .forEach(path -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                            FileTime creationTime = attr.creationTime();
                            File file = path.toFile();

                            playerDirs.add(file);
                            timeToFile.put(creationTime, file);
                            creationTimes.add(creationTime);
                        } catch (IOException e) {
                            GoodLogger.warn("Could not read attributes for: " + path.getFileName());
                        }
                    });
        } catch (IOException e) {
            GoodLogger.warn("Error reading directory: " + e.getMessage());
            return;
        }
        if (playerDirs.size() <= max) return;
        creationTimes.sort(FileTime::compareTo);
        int toDelete = playerDirs.size() - max;
        int deleted = 0;

        for (FileTime time : creationTimes) {
            if (deleted >= toDelete) break;

            File playerDir = timeToFile.get(time);
            if (playerDir == null) continue;

            // Delete directory recursively
            if (FileManager.deleteFolder(playerDir)) {
                GoodLogger.debug("Deleted old player data: " + playerDir.getAbsolutePath());
                deleted++;
            } else {
                GoodLogger.warn("Failed to delete: " + playerDir.getAbsolutePath());
            }
        }
    }
}
