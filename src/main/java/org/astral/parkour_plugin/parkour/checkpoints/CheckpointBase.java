package org.astral.parkour_plugin.parkour.checkpoints;

import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.config.maps.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public final class CheckpointBase {

    private final static Main plugin = Main.getInstance();

    private static final Map<String, Set<Checkpoint>> checkpointMap = new HashMap<>();
    private static final Map<Player, Checkpoint> actualCheckpoint = new HashMap<>();

    public static void loadMap(final String map){
        final CheckpointConfig checkpointConfig = new CheckpointConfig(map);
        for (final String key : checkpointConfig.keys()){
            try {
                checkpointConfig.getCheckpoint(key);
            }catch (IOException e) {
                plugin.getLogger().warning("No se pudo encontrar el checkpoint: "+ key);
            }
            final Checkpoint checkpoint = createCheckpoint(checkpointConfig);
            checkpointMap.computeIfAbsent(map, k -> new HashSet<>()).add(checkpoint);
        }
    }

    public static @NotNull Checkpoint createCheckpoint(final @NotNull CheckpointConfig checkpointConfig){
        final Location location = checkpointConfig.getLocation();
        Checkpoint checkpoint = new Checkpoint(location);
        checkpoint.setMaxY(checkpointConfig.getMaxFallY());
        checkpoint.setMinY(checkpointConfig.getMinFallY());
        return checkpoint;
    }

    public static void addPlayerLastCheckpoint(final Player player, final Checkpoint checkpoint){
        actualCheckpoint.put(player, checkpoint);
    }

    public static Checkpoint getLastCheckpointPlayer(final Player player){
        return actualCheckpoint.get(player);
    }

    public static Set<Checkpoint> getCheckpoints(final String key){
        return checkpointMap.get(key);
    }

    public static boolean isEqualLocation(final Location l1, final Location l2) {
        if (l1 == null || l2 == null) return false;
        if (l1.getWorld() == null || l2.getWorld() == null) return false;

        return l1.getWorld().equals(l2.getWorld()) &&
                l1.getBlockX() == l2.getBlockX() &&
                l1.getBlockY() == l2.getBlockY() &&
                l1.getBlockZ() == l2.getBlockZ();
    }

    public static double getMaxGeneralY(final String name_map, final  World world){
        return new Rules(name_map).getMaxY(world);
    }

    public static double getMinGeneralY(final String name_map, final World world){
        return new Rules(name_map).getMinY(world);
    }

}
