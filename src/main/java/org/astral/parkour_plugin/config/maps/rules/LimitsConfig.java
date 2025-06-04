package org.astral.parkour_plugin.config.maps.rules;

import org.astral.parkour_plugin.compatibilizer.adapters.LimitsWorldApi;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class LimitsConfig {

    private final YamlConfiguration yamlConfiguration;

    public LimitsConfig(final YamlConfiguration yamlConfiguration) {
        this.yamlConfiguration = yamlConfiguration;
    }

    public double getMinY(final @NotNull World world) {
        String key = getKey(world.getEnvironment(), "min_y");
        if (yamlConfiguration.contains(key)) {
            return yamlConfiguration.getDouble(key);
        }
        return LimitsWorldApi.getMinY(world);
    }

    public double getMaxY(final @NotNull World world) {
        String key = getKey(world.getEnvironment(), "max_y");
        if (yamlConfiguration.contains(key)) {
            return yamlConfiguration.getDouble(key);
        }
        return LimitsWorldApi.getMaxY(world);
    }

    private String getKey(final @NotNull World.Environment environment, final @NotNull String prefix) {
        switch (environment) {
            case NORMAL:
                return prefix + "_overworld";
            case NETHER:
                return prefix + "_nether";
            case THE_END:
                return prefix + "_end";
            default:
                return prefix;
        }
    }
}