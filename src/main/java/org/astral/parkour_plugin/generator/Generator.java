package org.astral.parkour_plugin.generator;

import org.astral.parkour_plugin.config.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class Generator{
    //Instances
    private static final Main plugin = Main.getInstance();

    private static void generateConfiguration(final Location location, final @NotNull CheckpointConfig checkpointConfig){
        final String checkpoint = checkpointConfig.createNextCheckpointName();
        checkpointConfig.createCheckpoint(checkpoint);
        try {
            checkpointConfig.getCheckpoint(checkpoint);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        checkpointConfig.setLocation(location);
        checkpointConfig.setMinFallY(CheckpointConfig.MIN_Y);
        checkpointConfig.setAllMaxFallY(CheckpointConfig.MAX_Y);
    }

    public void generatePlatformFloor(final Location location, final int count, final String name_map) {
        final CheckpointConfig checkpointConfig = new CheckpointConfig(name_map);
        Kit.getRegionScheduler().execute(plugin, location, () -> {
            final World world = location.getWorld();
            if (world == null) {
                plugin.getLogger().info("World is null");
                return;
            }
            final float yaw = location.getYaw();
            final double yawRad = Math.toRadians(yaw);
            Location currentLocation = location.clone();
            double verticalOffset = 0;
            for (int i = 0; i < count; i++) {
                boolean blockPlaced = false;
                final int horizontalOffset = (i == 0) ? 1 : 3;
                final Location nextLocation = currentLocation.clone().add(
                        -horizontalOffset * Math.sin(yawRad),
                        verticalOffset,
                        horizontalOffset * Math.cos(yawRad)
                );
                final Block block = nextLocation.getBlock();
                if (block.getType() == Material.AIR) {
                    generateConfiguration(block.getLocation(), checkpointConfig);
                    block.setType(Material.STONE);
                    //System.out.println(nextLocation);
                    currentLocation = nextLocation;
                    blockPlaced = true;
                    verticalOffset = 1;
                }
                if (!blockPlaced) {
                    plugin.getLogger().info("No se encontró una ubicación adecuada para el bloque " + i);
                    break;
                }
            }
        });
    }
}