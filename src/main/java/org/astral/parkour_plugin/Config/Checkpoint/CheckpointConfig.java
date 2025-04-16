package org.astral.parkour_plugin.Config.Checkpoint;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class CheckpointConfig extends BaseCheckpoint {

    public static final Double MIN_Y = -255.00;
    public static final Double MAX_Y = 255.00;
    public static final String CheckpointStructureName = BaseCheckpoint.CheckpointStructure;

    public CheckpointConfig(final String MAP_FOLDER) {
        super(MAP_FOLDER);
    }


    // Modifiable
    public @NotNull String createNextCheckpointName(){
        final String c = CheckpointStructureName;
        byte i = 1;
        while (yamlConfiguration.contains(c+i)) i++;
        return c+i;
    }

    // Getters
    public @NotNull Set<String> keys() {
        return yamlConfiguration.getKeys(false);
    }

    public boolean isArea(){
        validateConfigurationSection();
        return configurationSection.getKeys(false).contains("position_");
    }

    public int get_radius(){
        validateConfigurationSection();
        return configurationSection.getInt("radius");
    }

    public double getMinFallY(){
        validateConfigurationSection();
        return configurationSection.getDouble("min_fall_y",MIN_Y);
    }

    public double getMaxFallY(){
        validateConfigurationSection();
        return configurationSection.getDouble("max_fall_y", MAX_Y);
    }

    public @NotNull Location getLocation(){
        validateConfigurationSection();
        final World world = Bukkit.getWorld(Objects.requireNonNull(configurationSection.getString("world")));
        final double x = configurationSection.getDouble("x");
        final double y = configurationSection.getDouble("y");
        final double z = configurationSection.getDouble("z");
        return new Location(world, x, y, z);
    }

    public @NotNull List<Location> getLocations() {
        validateConfigurationSection();
        final List<Location> positions = new ArrayList<>();
        final World world = Bukkit.getWorld(Objects.requireNonNull(configurationSection.getString("world")));
        for (final String key : configurationSection.getKeys(false)) {
            if (key.startsWith("position_")) {
                final double x = configurationSection.getDouble(key + ".x");
                final double y = configurationSection.getDouble(key + ".y");
                final double z = configurationSection.getDouble(key + ".z");
                positions.add(new Location(world, x, y, z));
            }
        }
        return positions;
    }

    // SETTERS
    public void setRadius(final int radius){
        validateConfigurationSection();
        configurationSection.set("radius", radius);
        saveConfiguration();
    }

    public void setMinFallY(final double min_fall_y) {
        validateConfigurationSection();
        if (min_fall_y > getMaxFallY()) {
            throw new IllegalArgumentException("El valor de Minimo no puede superar el Maximo.");
        }
        configurationSection.set("min_fall_y", min_fall_y);
        saveConfiguration();
    }

    public void setMaxFallY(final double max_fall_y){
        validateConfigurationSection();
        if (max_fall_y < getMaxFallY()) {
            throw new IllegalArgumentException("El valor de Maximo no puede ser menor que el Minimo.");
        }
        configurationSection.set("max_fall_y", max_fall_y);
        saveConfiguration();
    }

    public boolean isEqualsLocation(final @NotNull Location location) {
        final Location comparator = getLocation();
        final double adjustedX = (int) location.getX() + 0.5;
        final double adjustedZ = (int) location.getZ() + 0.5;
        return comparator.getWorld().equals(location.getWorld()) &&
                comparator.getX() == adjustedX &&
                comparator.getY() == location.getY() &&
                comparator.getZ() == adjustedZ;
    }

    public void setLocation(final @NotNull Location location) {
        validateConfigurationSection();
        final double x = (int) location.getX() + 0.5;
        final double y = location.getBlockY();
        final double z = (int) location.getZ() + 0.5;
        configurationSection.set("world", location.getWorld().getName());
        configurationSection.set("x", x);
        configurationSection.set("y", y);
        configurationSection.set("z", z);
        saveConfiguration();
    }

    public void setLocations(final @NotNull List<Location> positions) {
        validateConfigurationSection();

        for (int i = 0; i < positions.size(); i++) {
            final Location loc = positions.get(i);
            final double x = Math.floor(loc.getX()) + 0.5;
            final double y = Math.floor(loc.getY());
            final double z = Math.floor(loc.getZ()) + 0.5;
            configurationSection.set("world", loc.getWorld().getName());
            configurationSection.set("position_" + i + ".x", x);
            configurationSection.set("position_" + i + ".y", y);
            configurationSection.set("position_" + i + ".z", z);
        }
        saveConfiguration();
    }

    public void setAllRadius(final int radius){
        for (final String checkpoint : yamlConfiguration.getKeys(false)){
            yamlConfiguration.set(checkpoint+"radius",radius);
        }
        saveConfiguration();
    }

    public void setAllMinFallY(final double all_y) {
        for (final String checkpoint : yamlConfiguration.getKeys(false)) {
            ConfigurationSection section = yamlConfiguration.getConfigurationSection(checkpoint);
            if (section != null) {
                double maxFallY = section.getDouble("max_fall_y", MIN_Y);
                if (all_y > maxFallY) {
                    throw new IllegalArgumentException("El valor de Minimo no puede superar el Maximo en el checkpoint: " + checkpoint);
                }
                section.set("min_fall_y", all_y);
            }
        }
        saveConfiguration();
    }

    public void setAllMaxFallY(final double all_y) {
        for (final String checkpoint : yamlConfiguration.getKeys(false)) {
            ConfigurationSection section = yamlConfiguration.getConfigurationSection(checkpoint);
            if (section != null) {
                double minFallY = section.getDouble("min_fall_y", MAX_Y);
                if (all_y < minFallY) {
                    throw new IllegalArgumentException("El valor de Maximo no puede ser menor que el Minimo en el checkpoint: " + checkpoint);
                }
                section.set("max_fall_y", all_y);
            }
        }
        saveConfiguration();
    }
}