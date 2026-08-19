package dev.nahum.nahumInventoryStuff;

import java.io.File;
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
}
