package org.astral.parkour_plugin.config.checkpoint;

import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BaseCheckpoint {

    // Instances
    protected static final Main plugin = Main.getInstance();
    protected final Configuration configuration = plugin.getConfiguration();

    // FOLDERS & FILES
    private final String MAPS = Configuration.MAPS;
    private final String MAP_FOLDER;
    protected static final String CHECKPOINT = Configuration.CHECKPOINT;

    // Configuration
    protected YamlConfiguration yamlConfiguration;
    protected ConfigurationSection configurationSection;

    //Structures
    protected static final String CheckpointStructure = "Checkpoint_";

    protected BaseCheckpoint(final String MAP_FOLDER) {
        this.MAP_FOLDER = MAP_FOLDER;
        try {
            yamlConfiguration = configuration.getYamlConfiguration(MAPS, this.MAP_FOLDER, CHECKPOINT);
        } catch (FileNotFoundException e) {
            plugin.getLogger().warning("YAML file not found for " + this.MAP_FOLDER + ".");
        }
    }

    public void createCheckpoint(final String checkpoint) {
        configurationSection = yamlConfiguration.getConfigurationSection(checkpoint);
        if (configurationSection != null) {
            plugin.getLogger().warning("Checkpoint " + checkpoint + " ya existe. No se creará nuevamente.");
            return;
        }
        configurationSection = yamlConfiguration.createSection(checkpoint);
        plugin.getLogger().info("Checkpoint " + checkpoint + " creado exitosamente.");
        configurationSection.set("fall_damage", false);
        configurationSection.set("damage_fire", false);
        saveConfiguration();
    }

    public void getCheckpoint(final String checkpoint) throws IOException {
        configurationSection = yamlConfiguration.getConfigurationSection(checkpoint);
        if (configurationSection == null) {
            throw new IOException("Checkpoint " + checkpoint + " no existe en el archivo de configuración.");
        }
    }

    public void deleteCheckpoint(final String checkpoint) {
        configurationSection = yamlConfiguration.getConfigurationSection(checkpoint);
        if (configurationSection == null) {
            plugin.getLogger().warning("Checkpoint " + checkpoint + " no existe. No se puede eliminar.");
            return;
        }
        yamlConfiguration.set(checkpoint, null);
        plugin.getLogger().info("Checkpoint " + checkpoint + " eliminado exitosamente.");
        saveConfiguration();
    }

    //MOD
    public void ChangePositionsCheckpoint(final Map<Integer, String> checkpointNames) {
        if (yamlConfiguration == null || checkpointNames.isEmpty()) return;
        final TreeMap<String, Map<String, Object>> originalCheckpoints = new TreeMap<>();
        yamlConfiguration.getKeys(false).stream()
                .filter(key -> key.startsWith(CheckpointStructure))
                .forEach(key -> {
                    final Map<String, Object> section = Objects.requireNonNull(yamlConfiguration.getConfigurationSection(key)).getValues(false);
                    originalCheckpoints.put(key, section);
                });
        yamlConfiguration.getKeys(false).stream()
                .filter(key -> key.startsWith(CheckpointStructure))
                .forEach(key -> yamlConfiguration.set(key, null));
        final TreeMap<String, Map<String, Object>> updatedCheckpoints = new TreeMap<>();
        checkpointNames.forEach((index, oldKey) -> {
            final String newKey = CheckpointStructure + (index + 1);
            if (originalCheckpoints.containsKey(oldKey)) {
                updatedCheckpoints.put(newKey, originalCheckpoints.get(oldKey));
            }
        });
        updatedCheckpoints.forEach((key, value) -> yamlConfiguration.createSection(key, value));
        saveConfiguration();
    }

    public void reorderCheckpoints() {
        final List<String> currentCheckpoints = yamlConfiguration.getKeys(false).stream()
                .filter(key -> key.startsWith(CheckpointStructure))
                .sorted(Comparator.comparingInt(key -> Integer.parseInt(key.split("_")[1])))
                .collect(Collectors.toList());
        final Map<String, Map<String, Object>> checkpointData = new HashMap<>();
        for (String checkpoint : currentCheckpoints) {
            checkpointData.put(checkpoint, Objects.requireNonNull(yamlConfiguration.getConfigurationSection(checkpoint)).getValues(false));
            yamlConfiguration.set(checkpoint, null);
        }
        int newIndex = 1;
        for (String oldCheckpoint : currentCheckpoints) {
            String newCheckpointName = CheckpointStructure + newIndex++;
            yamlConfiguration.createSection(newCheckpointName, checkpointData.get(oldCheckpoint));
        }
        saveConfiguration();
        plugin.getLogger().info("Checkpoints reordenados correctamente.");
    }


    // GETTERS
    public boolean Fall_Damage(){
        validateConfigurationSection();
        return configurationSection.getBoolean("fall_damage",false);
    }

    public boolean Damage_Fire(){
        validateConfigurationSection();
        return configurationSection.getBoolean("damage_fire", false);
    }

    // SETTERS
    public void Set_Fall_Damage(final boolean fall_damage){
        validateConfigurationSection();
        configurationSection.set("fall_damage", fall_damage);
        saveConfiguration();
    }

    public void Set_Damage_Fire(final boolean damage_fire){
        validateConfigurationSection();
        configurationSection.set("damage_fire", damage_fire);
    }

    // PROTECTED
    protected void validateConfigurationSection(){
        if (configurationSection == null || configurationSection.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("Configuration section is Empty. Make sure to set a valid checkpoint before accessing its data.");
        }
    }

    protected void saveConfiguration() {
        try {
            configuration.saveConfiguration(yamlConfiguration, MAPS, MAP_FOLDER, CHECKPOINT);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }
    }
}