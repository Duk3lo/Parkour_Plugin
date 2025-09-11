package org.astral.parkour_plugin.parkour.checkpoints;

import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.config.maps.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.config.maps.items.ParkourItem;
import org.astral.parkour_plugin.config.maps.items.ParkourItemType;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public final class CheckpointBase {

    private final static Main plugin = Main.getInstance();

    private static final Map<String, List<Checkpoint>> checkpointMap = new HashMap<>();
    private static final Map<UUID, Checkpoint> actualCheckpoint = new HashMap<>();
    private static final Map<String, Map<ParkourItemType, ParkourItem>> itemsRef = new HashMap<>();

    public static void setItemsRef(String map, Map<ParkourItemType, ParkourItem> ref){
        itemsRef.computeIfAbsent(map, k->ref);
    }

    public static void loadMap(final String map) {
        final CheckpointConfig checkpointConfig = new CheckpointConfig(map);
        byte id = 1;
        for (final String key : checkpointConfig.keys()) {
            try {
                checkpointConfig.getCheckpoint(key);
            } catch (IOException e) {
                plugin.getLogger().warning("No se pudo encontrar el checkpoint: " + key);
            }
            final Checkpoint checkpoint = createCheckpoint(checkpointConfig, id++, map);
            checkpointMap.computeIfAbsent(map, k -> new ArrayList<>()).add(checkpoint);
        }
    }

    public static @NotNull Checkpoint createCheckpoint(final @NotNull CheckpointConfig checkpointConfig, final byte id, String map) {
        final Location location = checkpointConfig.getLocation();
        Checkpoint checkpoint = new Checkpoint(location, id);
        Map<ParkourItemType, ParkourItem> itemsAlm = checkpointConfig.getItems(itemsRef.get(map));
        checkpoint.setMaxY(checkpointConfig.getMaxFallY());
        checkpoint.setMinY(checkpointConfig.getMinFallY());
        checkpoint.setItemMap(itemsAlm);
        return checkpoint;
    }

    public static void addPlayerLastCheckpoint(final UUID uuid, final Checkpoint checkpoint){
        actualCheckpoint.put(uuid, checkpoint);
    }

    public static Checkpoint getLastCheckpointPlayer(UUID uuid){
        return actualCheckpoint.get(uuid);
    }

    public static List<Checkpoint> getCheckpoints(final String key){
        return checkpointMap.get(key);
    }

    public static void removeCheckpoints(final String key){
        checkpointMap.remove(key);
    }

    public static boolean isEqualLocation(final Location l1, final Location l2) {
        if (l1 == null || l2 == null) return false;
        if (l1.getWorld() == null || l2.getWorld() == null) return false;

        return l1.getWorld().equals(l2.getWorld()) &&
                (int) l1.getX() == (int) l2.getX() &&
                (int) l1.getY() == (int) l2.getY() &&
                (int) l1.getZ() == (int) l2.getZ();
    }

    public static double getMaxGeneralY(final String name_map, final  World world){
        return new Rules(name_map).getMaxY(world);
    }

    public static double getMinGeneralY(final String name_map, final World world){
        return new Rules(name_map).getMinY(world);
    }

}
