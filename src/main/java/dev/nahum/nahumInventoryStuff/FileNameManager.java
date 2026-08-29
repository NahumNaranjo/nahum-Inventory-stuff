package dev.nahum.nahumInventoryStuff;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileNameManager {
    public static File getBackupName(File folder) {
        File returning;
        LocalDateTime now = LocalDateTime.now();
        returning = new File(folder.toPath() +
                File.separator + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".nahumbackup");

        for (int i = 1; returning.exists(); i++) {
            returning = new File(folder.toPath() +
                    File.separator + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                    "-(" + i + ")" + ".nahumbackup");
        }
        return returning;
    }

    public static String checkCustomBackupName(String input) {
        File backupFolder = PathManager.getBackupFolder();

        Path path = Paths.get(backupFolder.toString(), input);

        if (Files.exists(path) && Files.isRegularFile(path)) {
            return input;
        }

        if (!input.endsWith(".nahumbackup")) {
            Path withExtension = Paths.get(backupFolder.toString(), input + ".nahumbackup");
            if (Files.exists(withExtension) && Files.isRegularFile(withExtension)) {
                return input + ".nahumbackup";
            }
        }

        GoodLogger.error("Couldn't find backup file: " + input);
        return null;
    }
}
