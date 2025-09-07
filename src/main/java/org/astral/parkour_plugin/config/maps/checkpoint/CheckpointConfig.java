package org.astral.parkour_plugin.config.maps.checkpoint;

import org.astral.parkour_plugin.config.maps.items.ParkourItemType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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
    public @NotNull List<String> keys() {
        return yamlConfiguration.getKeys(false).stream()
                .filter(key -> key.startsWith(CheckpointStructureName))
                .sorted(Comparator.comparingInt(key -> {
                    try {
                        return Integer.parseInt(key.replaceAll("\\D+", ""));
                    } catch (NumberFormatException e) {
                        return Integer.MAX_VALUE;
                    }
                }))
                .collect(Collectors.toList());
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
        final ConfigurationSection locationsSection = configurationSection.getConfigurationSection("locations");
        if (locationsSection == null) return positions;
        for (String key : locationsSection.getKeys(false)) {
            final ConfigurationSection posSection = locationsSection.getConfigurationSection(key);
            if (posSection == null) continue;
            final String worldName = posSection.getString("world");
            final World world = Bukkit.getWorld(worldName);
            if (world == null) continue;
            double x = posSection.getDouble("x");
            double y = posSection.getDouble("y");
            double z = posSection.getDouble("z");

            positions.add(new Location(world, x, y, z));
        }
        return positions;
    }

    // SETTERS
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

    public void setDefaultItems() {
        List<String> checkpoints = keys();
        if (checkpoints.isEmpty()) return;
        String firstCheckpoint = checkpoints.get(0);
        if (!configurationSection.getName().equals(firstCheckpoint)) {
            return;
        }
        final String name = "items." + ParkourItemType.LAST_CHECKPOINT.name();
        // Defaults
        configurationSection.set(name + ".modifyItem", false);
        configurationSection.set(name + ".removeItem", false);
        configurationSection.set(name + ".slot", 0);
        configurationSection.set(name + ".material", "EMERALD");
        configurationSection.set(name + ".displayName", "§aCheckpoint Especial");
        configurationSection.set(name + ".lore", Arrays.asList("§7Este checkpoint tiene", "§7propiedades personalizadas."));
        configurationSection.set(name + ".cooldown", 0);
        configurationSection.set(name + ".uses", -1);
        configurationSection.set(name + ".accumulateUses", false);
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
        setDefaultItems();
        saveConfiguration();
    }

    public void setLocations(final @NotNull List<Location> positions) {
        validateConfigurationSection();
        configurationSection.set("locations", null);
        final ConfigurationSection locationsSection = configurationSection.createSection("locations");
        for (int i = 0; i < positions.size(); i++) {
            final Location loc = positions.get(i);
            final double x = Math.floor(loc.getX()) + 0.5;
            final double y = Math.floor(loc.getY());
            final double z = Math.floor(loc.getZ()) + 0.5;
            final ConfigurationSection posSection = locationsSection.createSection("position_" + i);
            posSection.set("world", loc.getWorld().getName());
            posSection.set("x", x);
            posSection.set("y", y);
            posSection.set("z", z);
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