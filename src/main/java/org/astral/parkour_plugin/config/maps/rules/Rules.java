package org.astral.parkour_plugin.config.maps.rules;

import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Rules {
    // Instances
    private static final Main plugin = Main.getInstance();
    private final Configuration configuration = plugin.getConfiguration();

    // FOLDERS & FILES
    private final String MAPS = Configuration.MAPS;
    private final String MAP_FOLDER;
    // Configuration
    private YamlConfiguration yamlConfiguration;

    // File Names
    public static final String RULES_YML = Configuration.RULES_YML;

    // Reserved
    public static final String spawnPointsKey = "spawns_points";
    public static final String endPointsKey = "end_points";

    public Rules(final String MAP_FOLDER){
        this.MAP_FOLDER = MAP_FOLDER;
        try {
            yamlConfiguration = configuration.getYamlConfiguration(MAPS, this.MAP_FOLDER, RULES_YML);
        } catch (FileNotFoundException e) {
            plugin.getLogger().warning("YAML file not found for " + this.MAP_FOLDER + ".");
        }
    }

    public @NotNull List<Location> getSpawnsLocations() {
        final List<Location> positions = new ArrayList<>();
        final ConfigurationSection spawnsSection = yamlConfiguration.getConfigurationSection(spawnPointsKey);
        if (spawnsSection == null) return positions;
        for (String key : spawnsSection.getKeys(false)) {
            final ConfigurationSection positionSection = spawnsSection.getConfigurationSection(key);
            if (positionSection == null) continue;
            final String worldName = positionSection.getString("world");
            final World world = Bukkit.getWorld(worldName);
            if (world == null) continue;
            double x = positionSection.getDouble("x");
            double y = positionSection.getDouble("y");
            double z = positionSection.getDouble("z");
            positions.add(new Location(world, x, y, z));
        }
        return positions;
    }

    public @NotNull List<Location> getEndPoints() {
        final List<Location> positions = new ArrayList<>();
        final ConfigurationSection finishSection = yamlConfiguration.getConfigurationSection(endPointsKey);
        if (finishSection == null) return positions;
        for (String key : finishSection.getKeys(false)) {
            final ConfigurationSection positionSection = finishSection.getConfigurationSection(key);
            if (positionSection == null) continue;
            final String worldName = positionSection.getString("world");
            final World world = Bukkit.getWorld(worldName);
            if (world == null) continue;
            final double x = positionSection.getDouble("x");
            final double y = positionSection.getDouble("y");
            final double z = positionSection.getDouble("z");
            positions.add(new Location(world, x, y, z));
        }
        return positions;
    }

    public @NotNull String setSpawns(final @NotNull Location location) {
        final List<Location> positions = getSpawnsLocations();
        positions.add(location);
        ConfigurationSection spawnsSection = yamlConfiguration.getConfigurationSection(spawnPointsKey);
        if (spawnsSection == null)
            spawnsSection = yamlConfiguration.createSection(spawnPointsKey);

        final int nextIndex = spawnsSection.getKeys(false).size();
        final String val = "position_" + nextIndex;
        final ConfigurationSection section = spawnsSection.createSection(val);
        section.set("world", location.getWorld().getName());
        section.set("x", Math.floor(location.getX()) + 0.5);
        section.set("y", Math.floor(location.getY()));
        section.set("z", Math.floor(location.getZ()) + 0.5);
        saveConfiguration();
        return val;
    }

    public @NotNull String setEndPoints(final @NotNull Location location) {
        final List<Location> positions = getEndPoints();
        positions.add(location);
        ConfigurationSection finishSection = yamlConfiguration.getConfigurationSection(endPointsKey);
        if (finishSection == null)
            finishSection = yamlConfiguration.createSection(endPointsKey);
        final int nextIndex = finishSection.getKeys(false).size();
        final String val = "position_" + nextIndex;
        final ConfigurationSection section = finishSection.createSection(val);
        section.set("world", location.getWorld().getName());
        section.set("x", Math.floor(location.getX()) + 0.5);
        section.set("y", Math.floor(location.getY()));
        section.set("z", Math.floor(location.getZ()) + 0.5);
        saveConfiguration();
        return val;
    }

    private @NotNull String getNextPositionKey(@NotNull String sectionKey) {
        ConfigurationSection section = yamlConfiguration.getConfigurationSection(sectionKey);
        if (section == null) {
            return "position_0";
        }

        int maxIndex = -1;
        for (String key : section.getKeys(false)) {
            if (key.startsWith("position_")) {
                try {
                    int index = Integer.parseInt(key.substring("position_".length()));
                    if (index > maxIndex) {
                        maxIndex = index;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return "position_" + (maxIndex + 1);
    }

    public void removePoint(@NotNull Location location) {
        List<Location> spawns = getSpawnsLocations();
        List<Location> ends = getEndPoints();
        final double targetX = Math.floor(location.getX()) + 0.5;
        final double targetY = location.getY();
        final double targetZ = Math.floor(location.getZ()) + 0.5;
        final World world = location.getWorld();
        boolean removed = false;
        Iterator<Location> it = spawns.iterator();
        while (it.hasNext()) {
            Location loc = it.next();
            if (loc.getWorld().equals(world) &&
                    loc.getX() == targetX &&
                    loc.getY() == targetY &&
                    loc.getZ() == targetZ) {
                it.remove();
                removed = true;
                break;
            }
        }
        if (removed) {
            yamlConfiguration.set(spawnPointsKey, null);
            ConfigurationSection spawnsSection = yamlConfiguration.createSection(spawnPointsKey);
            for (int i = 0; i < spawns.size(); i++) {
                Location loc = spawns.get(i);
                ConfigurationSection section = spawnsSection.createSection("position_" + i);
                section.set("world", loc.getWorld().getName());
                section.set("x", Math.floor(loc.getX()) + 0.5);
                section.set("y", Math.floor(loc.getY()));
                section.set("z", Math.floor(loc.getZ()) + 0.5);
            }
            saveConfiguration();
            return;
        }
        it = ends.iterator();
        while (it.hasNext()) {
            Location loc = it.next();
            if (loc.getWorld().equals(world) &&
                    loc.getX() == targetX &&
                    loc.getY() == targetY &&
                    loc.getZ() == targetZ) {
                it.remove();
                removed = true;
                break;
            }
        }
        if (removed) {
            yamlConfiguration.set(endPointsKey, null);
            ConfigurationSection endSection = yamlConfiguration.createSection(endPointsKey);
            for (int i = 0; i < ends.size(); i++) {
                Location loc = ends.get(i);
                ConfigurationSection section = endSection.createSection("position_" + i);
                section.set("world", loc.getWorld().getName());
                section.set("x", Math.floor(loc.getX()) + 0.5);
                section.set("y", Math.floor(loc.getY()));
                section.set("z", Math.floor(loc.getZ()) + 0.5);
            }
            saveConfiguration();
        }
    }

    public boolean isEqualsLocation(final @NotNull Location location) {
        List<Location> allPoints = new ArrayList<>(getSpawnsLocations());
        allPoints.addAll(getEndPoints());
        final double adjustedX = (int) location.getX() + 0.5;
        final double adjustedZ = (int) location.getZ() + 0.5;
        final double y = location.getY();
        final World world = location.getWorld();

        for (Location end : allPoints) {
            if (end.getWorld().equals(world) &&
                    end.getX() == adjustedX &&
                    end.getY() == y &&
                    end.getZ() == adjustedZ) {
                return true;
            }
        }
        return false;
    }

    private void saveConfiguration() {
        try {
            configuration.saveConfiguration(yamlConfiguration, MAPS, MAP_FOLDER , RULES_YML);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }
    }
}
