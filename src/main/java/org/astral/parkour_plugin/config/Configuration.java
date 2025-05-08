package org.astral.parkour_plugin.config;

import org.astral.parkour_plugin.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Configuration {
    // Instances
    private static final Main plugin = Main.getInstance();

    // Folder Name
    public static final String MAPS = "Maps";
    public static final String CACHE = "Cache";
    private static final String MAP_FOLDER = "Example";


    // File Names
    public static final String CONFIG = "config.yml";
    public static final String RULES = "Rules.yml";
    public static final String CHECKPOINT = "Checkpoints.yml";

    //Extensions
    public static final String YML = ".yml";
    public static final String JSON = ".json";

    // Files of Example
    public static final File FOLDER_PLUGIN = plugin.getDataFolder();
    private static final File CONFIGURATION_FILE = new File(FOLDER_PLUGIN, CONFIG);
    private static final File CHECKPOINT_FILE = new File(FOLDER_PLUGIN, MAPS + File.separator + MAP_FOLDER + File.separator + CHECKPOINT);
    private static final File RULES_FILE = new File(FOLDER_PLUGIN, MAPS + File.separator + MAP_FOLDER + File.separator + RULES);

    public Configuration(){
        if (!CONFIGURATION_FILE.exists()) plugin.saveResource(CONFIG, false);
        if (!CHECKPOINT_FILE.exists()) plugin.saveResource(MAPS + File.separator + MAP_FOLDER + File.separator + CHECKPOINT, false);
        if (!RULES_FILE.exists()) plugin.saveResource(MAPS + File.separator + MAP_FOLDER + File.separator + RULES, false);
    }

    public static void createMapFolder(final @NotNull String mapFolder) {
        final List<String> RESERVED_NAMES = Collections.unmodifiableList(Arrays.asList(
                "com", "com1", "aux", "nul", "prn", "con",
                "lpt1", "lpt2", "lpt3", "clock$", "config$", "desktop$"
        ));
        if (RESERVED_NAMES.contains(mapFolder.toLowerCase())) {
            plugin.getLogger().severe("Error: The map name '" + mapFolder + "' is reserved and cannot be used.");
            return;
        }
        if (!mapFolder.matches("^[a-zA-Z0-9_\\- ()]{3,50}$")){
            plugin.getLogger().severe("Error: The map name '" + mapFolder + "' contains invalid characters or is too short/long.");
            return;
        }
        final File checkpointFile = new File(FOLDER_PLUGIN, MAPS + File.separator + mapFolder + File.separator + CHECKPOINT);
        final File rulesFile = new File(FOLDER_PLUGIN, MAPS + File.separator + mapFolder + File.separator + RULES);
        try {
            File parentDir = checkpointFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Failed to create directories for: " + parentDir.getAbsolutePath());
            }
            if (!checkpointFile.exists()) {
                if (checkpointFile.createNewFile()) {
                    plugin.getLogger().info("Created file: " + checkpointFile.getAbsolutePath());
                } else {
                    throw new IOException("Failed to create file: " + checkpointFile.getAbsolutePath());
                }
            }
            if (!rulesFile.exists()) {
                if (rulesFile.createNewFile()) {
                    plugin.getLogger().info("Created file: " + rulesFile.getAbsolutePath());
                } else {
                    throw new IOException("Failed to create file: " + rulesFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Error initializing configuration files: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: ");
            for (StackTraceElement element : e.getStackTrace()) {
                plugin.getLogger().severe("at " + element);
            }
        }
    }

    public static void deleteMapFolder(final @NotNull String mapFolder) {
        if (mapFolder.trim().isEmpty()) {
            plugin.getLogger().severe("Error: The map name cannot be null or empty.");
            return;
        }
        final File mapFolderFile = new File(FOLDER_PLUGIN, MAPS + File.separator + mapFolder);
        if (!mapFolderFile.exists() || !mapFolderFile.isDirectory()) {
            plugin.getLogger().severe("Error: The map folder '" + mapFolder + "' does not exist or is not a directory.");
            return;
        }
        final File[] files = mapFolderFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    plugin.getLogger().warning("Failed to delete file: " + file.getAbsolutePath());
                }
            }
        }
        if (mapFolderFile.delete()) {
            plugin.getLogger().info("Successfully deleted map folder: " + mapFolderFile.getAbsolutePath());
        } else {
            plugin.getLogger().severe("Failed to delete map folder: " + mapFolderFile.getAbsolutePath());
        }
    }

    public static String getUniqueFolderName(String folderName) {
        final File baseFolder = new File(FOLDER_PLUGIN, MAPS);
        File folder = new File(baseFolder, folderName);
        int counter = 1;
        while (folder.exists()) {
            folderName = folderName.replaceAll(" \\(\\d+\\)$", "") + " (" + counter + ")";
            folder = new File(baseFolder, folderName);
            counter++;
        }
        return folderName;
    }

    public static @NotNull List<String> getMaps() {
        final File folder = new File(FOLDER_PLUGIN, MAPS);
        if (!folder.exists() || !folder.isDirectory()) return Collections.emptyList();
        return Arrays.stream(Objects.requireNonNull(folder.listFiles(File::isDirectory)))
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public void saveConfiguration(final YamlConfiguration yml, final String @NotNull ... AbsolutePath) throws IOException {
        String lastPart = AbsolutePath[AbsolutePath.length - 1];
        if (!lastPart.endsWith(YML)) AbsolutePath[AbsolutePath.length - 1] = lastPart + YML;
        final String fullPath = String.join(File.separator, AbsolutePath);
        final File fileToSave = new File(FOLDER_PLUGIN, fullPath);
        final File parentDir = fileToSave.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) throw new IOException("Failed to create directories for path: " + parentDir.getAbsolutePath());
        yml.save(fileToSave);
    }

    public @NotNull YamlConfiguration getYamlConfiguration(final String @NotNull ... pathParts) throws FileNotFoundException {
        final String lastPart = pathParts[pathParts.length - 1];
        if (!lastPart.endsWith(YML)) pathParts[pathParts.length - 1] = lastPart + YML;
        final String fullPath = String.join(File.separator, pathParts);
        final File file = new File(FOLDER_PLUGIN, fullPath);
        if (!file.exists()) throw new FileNotFoundException("The configuration file " + fullPath + " was not found.");
        return YamlConfiguration.loadConfiguration(file);
    }
}