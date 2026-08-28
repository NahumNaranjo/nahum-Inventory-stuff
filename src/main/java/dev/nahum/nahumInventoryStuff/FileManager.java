package dev.nahum.nahumInventoryStuff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class FileManager {
    private FileManager() {
    } // No instantiation for anyone >:D

    public static File createFolder(String parent, String subfolder) {
        File f = new File(parent, subfolder);
        if (!f.exists()) {
            if (f.mkdirs()) {
                return f;
            } else {
                GoodLogger.warn("Failed to create " + subfolder + " folder!");
                return null;
            }
        } else {
            return f;
        }
    }

    public static boolean deleteFolder(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteFolder(file);
            }
        }
        return directory.delete();
    }

    public static boolean isFolderEmpty(File f) {
        try (Stream<Path> stream = Files.list(f.toPath())) {
            return stream.findAny().isEmpty();
        } catch (IOException e) {
            GoodLogger.warn("Failed to list player data!\nError: " + e);
            return true;
        }
    }

    public static String cleanName(String name) {
        if (name.endsWith(".nahumbackup")) {
            name = name.replace(".nahumbackup", "");
        }
        name = name.replaceAll("-\\(\\d+\\)$", "");
        return name;
    }

    public static LocalDate getLocalDateFromFileName(String fileName) {
        fileName = cleanName(fileName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime dateTime = LocalDateTime.parse(fileName, formatter);
        return dateTime.toLocalDate();
    }

    public static LocalDateTime getLocalDateTimeFromFileName(String fileName) {
        fileName = cleanName(fileName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return LocalDateTime.parse(fileName, formatter);
    }

    public static LocalDateTime getLocalDateTimeFromAtt(File file) {
        if(!file.exists()) return LocalDateTime.now();

        BasicFileAttributes attributes = getBasicFileAttributes(file);
        FileTime time = attributes.lastModifiedTime();

        return LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault());
    }

    public static List<File> getAllFiles(File folder) {
        List<File> files = new ArrayList<>();
        try (Stream<Path> stream = Files.list(folder.toPath())) {
            stream.forEach(path -> files.add(path.toFile()));
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
            return new ArrayList<>();
        }
        return files;
    }

    public static List<File> getAllRegularFiles(File folder) {
        List<File> files = new ArrayList<>();
        try (Stream<Path> stream = Files.list(folder.toPath())) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> files.add(path.toFile()));
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
            return new ArrayList<>();
        }
        return files;
    }

    public static List<File> getAllDirectories(File folder) {
        List<File> files = new ArrayList<>();
        try (Stream<Path> stream = Files.list(folder.toPath())) {
            stream.filter(Files::isDirectory)
                    .forEach(path -> files.add(path.toFile()));
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
            return new ArrayList<>();
        }
        return files;
    }

    public static BasicFileAttributes getBasicFileAttributes(File f) {
        return getBasicFileAttributes(f.toPath());
    }

    public static BasicFileAttributes getBasicFileAttributes(Path f) {
        try{
            return Files.readAttributes(f, BasicFileAttributes.class);
        } catch (IOException e){
            System.err.println("Error reading basic file attributes: " + e.getMessage());
            return null;
        }
    }

    public static File getOldestRegularFileFromName(File folder) {
        if(folder.isFile()) return null;
        return getOldestRegularFileFromName(getAllRegularFiles(folder));
    }

    public static File getOldestRegularFileFromName(List<File> contents) {
        return getOldestFile(contents, file -> getLocalDateTimeFromFileName(file.getName()));
    }

    public static File getOldestRegularFileFromAtt(File folder) {
        if(folder.isFile()) return null;
        return getOldestRegularFileFromAtt(getAllRegularFiles(folder));
    }

    public static File getOldestRegularFileFromAtt(List<File> contents) {
        return getOldestFile(contents, file -> getBasicFileAttributes(file).lastModifiedTime());
    }

    public static File getOldestFolderFromAtt(File folder) {
        return getOldestFolderFromAtt(folder, getAllDirectories(folder));
    }

    public static File getOldestFolderFromAtt(File folder, List<File> contents) {
        return getOldestFile(contents, file -> getBasicFileAttributes(file).lastModifiedTime());
    }

    private static <T extends Comparable<? super T>> File getOldestFile(
            List<File> contents, Function<File, T> dateExtractor) {
        Map<T, File> files = new HashMap<>();
        for (File file : contents) {
            files.put(dateExtractor.apply(file), file);
        }
        if (files.isEmpty()) return null;

        return files.entrySet().stream()
                .min(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    public static File getNewestFileFromAtt(String directoryPath) {
        File dir = new File(directoryPath);
        File[] files = dir.listFiles(File::isFile);

        if (files == null || files.length == 0) {
            return null;
        }

        Optional<File> newestFile = Arrays.stream(files)
                .max(Comparator.comparingLong(File::lastModified));

        return newestFile.orElse(null);
    }
}