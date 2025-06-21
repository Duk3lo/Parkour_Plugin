package org.astral.parkour_plugin.config.maps.rules;

import org.astral.parkour_plugin.compatibilizer.adapters.LimitsWorldApi;
import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
    private static final String spawnPointsKey = "spawns_points";
    private static final String endPointsKey = "end_points";

    public static final String spawnP = "spawn_";
    public static final String endP = "meta_";

    public Rules(final String MAP_FOLDER){
        this.MAP_FOLDER = MAP_FOLDER;
        try {
            yamlConfiguration = configuration.getYamlConfiguration(MAPS, this.MAP_FOLDER, RULES_YML);
        } catch (FileNotFoundException e) {
            plugin.getLogger().warning("YAML file not found for " + this.MAP_FOLDER + ".");
        }
    }

    public String getMapName(){
        return MAP_FOLDER;
    }

    public boolean isAutoReconnectEnabled() {
        return yamlConfiguration.getBoolean("auto_reconnect", true);
    }

    //Timer
    public boolean isTimerEnabled() {
        return yamlConfiguration.getBoolean("timer.enabled", true);
    }

    public boolean isActionBarTimerDisplayEnabled() {
        return yamlConfiguration.getBoolean("timer.display_actionbar", true);
    }

    public @NotNull String getTimerFormat() {
        return yamlConfiguration.getString("timer.format", "§fTiempo: §b{minutes}m {seconds}s");
    }

    public boolean isGlobalModeTime() {
        final String mode = yamlConfiguration.getString("timer.mode", "individual");
        return mode.equalsIgnoreCase("global");
    }

    public boolean isCountdownEnabled() {
        return yamlConfiguration.getBoolean("timer.countdown", false);
    }

    public int getTimeLimit() {
        return yamlConfiguration.getInt("timer.time_limit", -1);
    }

    public Optional<Title> getStartTitle() {
        ConfigurationSection titleSection = yamlConfiguration.getConfigurationSection("title");
        if (titleSection != null) {
            String main = titleSection.getString("main", "§a¡Parkour iniciado!");
            String subtitle = titleSection.getString("subtitle", "§fMapa: §b" + MAP_FOLDER)
                    .replace("{map}", MAP_FOLDER);
            int fadeIn = titleSection.getInt("fadeIn", 10);
            int stay = titleSection.getInt("stay", 40);
            int fadeOut = titleSection.getInt("fadeOut", 10);

            return Optional.of(new Title(main, subtitle, fadeIn, stay, fadeOut));
        }
        return Optional.empty();
    }

    public Optional<String> getMessage(final String key, final String player) {
        ConfigurationSection section = yamlConfiguration.getConfigurationSection("messages");
        if (section == null) return Optional.empty();

        String value = section.getString(key);
        if (value == null || value.trim().isEmpty()) return Optional.empty();

        value = value
                .replace("{player}", player)
                .replace("{map}", MAP_FOLDER);

        return Optional.of(value);
    }

    public double getMinY(@NotNull World world) {
        switch (world.getEnvironment()) {
            case NETHER:
                return yamlConfiguration.getDouble("min_y_nether", LimitsWorldApi.getMinY(world));
            case THE_END:
                return yamlConfiguration.getDouble("min_y_end", LimitsWorldApi.getMinY(world));
            case NORMAL:
            default:
                return yamlConfiguration.getDouble("min_y_overworld", LimitsWorldApi.getMinY(world));
        }
    }

    public double getMaxY(@NotNull World world) {
        switch (world.getEnvironment()) {
            case NETHER:
                return yamlConfiguration.getDouble("max_y_nether", LimitsWorldApi.getMaxY(world));
            case THE_END:
                return yamlConfiguration.getDouble("max_y_end", LimitsWorldApi.getMaxY(world));
            case NORMAL:
            default:
                return yamlConfiguration.getDouble("max_y_overworld", LimitsWorldApi.getMaxY(world));
        }
    }

    public @NotNull List<Location> getSpawnsPoints() {
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
        final List<Location> positions = getSpawnsPoints();
        positions.add(location);
        ConfigurationSection spawnsSection = yamlConfiguration.getConfigurationSection(spawnPointsKey);
        if (spawnsSection == null)
            spawnsSection = yamlConfiguration.createSection(spawnPointsKey);

        final int nextIndex = spawnsSection.getKeys(false).size();
        final String val = spawnP + nextIndex;
        final ConfigurationSection section = spawnsSection.createSection(val);
        final double x = (int) location.getX() + 0.5;
        final double y = location.getBlockY();
        final double z = (int) location.getZ() + 0.5;
        section.set("world", location.getWorld().getName());
        section.set("x", x);
        section.set("y", y);
        section.set("z", z);
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
        final String val = endP + nextIndex;
        final ConfigurationSection section = finishSection.createSection(val);
        section.set("world", location.getWorld().getName());
        section.set("x", Math.floor(location.getX()) + 0.5);
        section.set("y", Math.floor(location.getY()));
        section.set("z", Math.floor(location.getZ()) + 0.5);
        saveConfiguration();
        return val;
    }

    public void removeSpawnPoint(@NotNull Location location) {
        List<Location> spawns = getSpawnsPoints();
        final double targetX = (int) location.getX() + 0.5;
        final double targetY = location.getY();
        final double targetZ = (int) location.getZ() + 0.5;
        final World world = location.getWorld();

        Iterator<Location> it = spawns.iterator();
        boolean removed = false;
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
                ConfigurationSection section = spawnsSection.createSection(spawnP + i);
                section.set("world", loc.getWorld().getName());
                section.set("x", loc.getX());
                section.set("y", loc.getY());
                section.set("z", loc.getZ());
            }
            saveConfiguration();
        }
    }

    public void removeEndPoint(@NotNull Location location) {
        List<Location> ends = getEndPoints();
        final double targetX = (int) location.getX() + 0.5;
        final double targetY = location.getY();
        final double targetZ = (int) location.getZ() + 0.5;
        final World world = location.getWorld();

        Iterator<Location> it = ends.iterator();
        boolean removed = false;
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
                ConfigurationSection section = endSection.createSection(endP + i);
                section.set("world", loc.getWorld().getName());
                section.set("x", loc.getX());
                section.set("y", loc.getY());
                section.set("z", loc.getZ());
            }
            saveConfiguration();
        }
    }

    public @Nullable Location getSpawnLocationFromKey(@NotNull String key) {
        return getLocationFromKey(spawnPointsKey, key);
    }

    public @Nullable Location getEndPointLocationFromKey(@NotNull String key) {
        return getLocationFromKey(endPointsKey, key);
    }

    private @Nullable Location getLocationFromKey(@NotNull String sectionKey, @NotNull String key) {
        final ConfigurationSection section = yamlConfiguration.getConfigurationSection(sectionKey);
        if (section == null) return null;

        final ConfigurationSection posSection = section.getConfigurationSection(key);
        if (posSection == null) return null;

        final String worldName = posSection.getString("world");
        final double x = posSection.getDouble("x");
        final double y = posSection.getDouble("y");
        final double z = posSection.getDouble("z");

        final World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        return new Location(world, x, y, z);
    }

    public String getSpawnKeyFromLocation(final @NotNull Location location) {
        return getKeyFromLocation(spawnPointsKey, location);
    }

    public String getEndPointKeyFromLocation(final @NotNull Location location) {
        return getKeyFromLocation(endPointsKey, location);
    }

    private @Nullable String getKeyFromLocation(@NotNull String sectionKey, @NotNull Location location) {
        final ConfigurationSection section = yamlConfiguration.getConfigurationSection(sectionKey);
        if (section == null) return null;

        final double targetX = (int) location.getX() + 0.5;
        final double targetY = location.getBlockY();
        final double targetZ = (int) location.getZ() + 0.5;
        final String targetWorld = location.getWorld().getName();

        for (String key : section.getKeys(false)) {
            ConfigurationSection posSection = section.getConfigurationSection(key);
            if (posSection == null) continue;

            String world = posSection.getString("world");
            double x = posSection.getDouble("x");
            double y = posSection.getDouble("y");
            double z = posSection.getDouble("z");

            if (world != null && world.equals(targetWorld)
                    && x == targetX && y == targetY && z == targetZ) {
                return key;
            }
        }
        return null;
    }

    public boolean isEqualsLocation(final @NotNull Location location) {
        List<Location> allPoints = new ArrayList<>(getSpawnsPoints());
        allPoints.addAll(getEndPoints());

        final double adjustedX = (int) location.getX() + 0.5;
        final double adjustedZ = (int) location.getZ() + 0.5;
        location.setX(adjustedX);
        location.setZ(adjustedZ);
        for (Location end : allPoints) {
            if (end.getWorld().equals(location.getWorld()) &&
                    end.getX() == location.getX() &&
                    end.getY() == location.getBlockY() &&
                    end.getZ() == location.getZ()) {

                return true;
            }
        }
        return false;
    }


    public @NotNull String @NotNull [] getSpawnKeys() {
        final List<String> keys = new ArrayList<>();
        ConfigurationSection spawnsSection = yamlConfiguration.getConfigurationSection(spawnPointsKey);
        if (spawnsSection != null) {
            for (String key : spawnsSection.getKeys(false)) {
                if (key.startsWith(spawnP)) {
                    keys.add(key);
                }
            }
        }
        return keys.toArray(new String[0]);
    }

    public @NotNull String @NotNull [] getEndKeys() {
        final List<String> keys = new ArrayList<>();
        ConfigurationSection endSection = yamlConfiguration.getConfigurationSection(endPointsKey);
        if (endSection != null) {
            for (String key : endSection.getKeys(false)) {
                if (key.startsWith(endP)) {
                    keys.add(key);
                }
            }
        }
        return keys.toArray(new String[0]);
    }

    private void saveConfiguration() {
        try {
            configuration.saveConfiguration(yamlConfiguration, MAPS, MAP_FOLDER , RULES_YML);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }
    }
}
