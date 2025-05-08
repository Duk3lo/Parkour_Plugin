package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.config.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class Parkour {

    //Instances
    private final static Main Plugin = Main.getInstance();
    private final Configuration configuration = Plugin.getConfiguration();

    //Maps
    private final Map<String, List<Checkpoint>> checkpointsMap = new HashMap<>();
    private final Map<Player, Checkpoint> playerLastCheckpoint = new HashMap<>();
    private final Map<String, Set<Player>> playerParkourMap = new HashMap<>();
    private final Map<Player, Location> lastLocationCheckpoint = new HashMap<>();

    // Configuration
    private static @NotNull Checkpoint createCheckpointFromConfig(final @NotNull CheckpointConfig checkpointConfig) {
        boolean isArea = checkpointConfig.isArea();
        final Object area;
        if (isArea) area = checkpointConfig.getLocations();
        else area = checkpointConfig.getLocation();
        final double minFallY = checkpointConfig.getMinFallY();
        final double maxFallY = checkpointConfig.getMaxFallY();
        return new Checkpoint(area, minFallY, maxFallY, isArea);
    }

    public void loadCheckpoints() {
        for (final String maps : Configuration.getMaps()) {
            final List<Checkpoint> checkpoints = new ArrayList<>();
            final CheckpointConfig checkpointConfig = new CheckpointConfig(maps);
            for (final String key : checkpointConfig.keys()) {
                try {
                    checkpointConfig.getCheckpoint(key);
                } catch (IOException e) {
                    Plugin.getLogger().warning("Error al cargar el checkpoint '" + key + "' en el mapa '" + maps + "': " + e.getMessage());
                }
                final Checkpoint checkpoint = createCheckpointFromConfig(checkpointConfig);
                checkpoints.add(checkpoint);
            }
            checkpointsMap.put(maps, checkpoints);
        }
    }

    //Add
    public boolean addPlayerParkourMap(final String mapName, final Player player){
        return playerParkourMap.computeIfAbsent(mapName, V -> new HashSet<>()).add(player);
    }

    public void addPlayerOfLastCheckpoint(final Player player, final Location location){
        lastLocationCheckpoint.put(player, location);
    }

    //Remove
    public boolean removePlayerParkourMap(final String mapName, final Player player) {
        checkpointsMap.values().forEach(checkpoints -> {
            checkpoints.forEach(checkpoint -> {
                final Set<Player> players = checkpoint.getPlayers().get(mapName);
                if (players != null) {
                    players.removeIf(p -> p.equals(player));
                }
            });
        });
        final Set<Player> playersInMap = playerParkourMap.get(mapName);
        if (playersInMap != null && playersInMap.remove(player)) {
            lastLocationCheckpoint.remove(player);
            playerLastCheckpoint.remove(player);
            return true;
        }
        return false;
    }

    //Getters
    public Location getLastLocationOfPlayer(final Player player){
        return lastLocationCheckpoint.get(player);
    }

    public Set<Player> getPlayerOnParkour(){
        return playerParkourMap.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public Map<String, List<Checkpoint>> getCheckpointsMap(){
        return checkpointsMap;
    }

    public Map<Player, Checkpoint> getPlayerLastCheckpoint(){
        return playerLastCheckpoint;
    }

    public String getMapNameByPlayer(final Player player) {
        for (final Map.Entry<String, Set<Player>> entry : playerParkourMap.entrySet()) {
            if (entry.getValue().contains(player)) {
                return entry.getKey();
            }
        }
        return "";
    }

    //Statics
    public static boolean isLocationMatch(final @NotNull Location checkpointLocation, final @NotNull Location playerLocation) {
        return checkpointLocation.getWorld().equals(playerLocation.getWorld()) &&
                checkpointLocation.getBlockX() == playerLocation.getBlockX() &&
                checkpointLocation.getBlockY() == playerLocation.getBlockY() &&
                checkpointLocation.getBlockZ() == playerLocation.getBlockZ();
    }

    public static boolean isLocationMatch(final @NotNull List<Location> checkpointLocations, final @NotNull Location playerLocation) {
        for (final Location checkpointLocation : checkpointLocations) {
            if (checkpointLocation.getWorld().equals(playerLocation.getWorld()) &&
                    checkpointLocation.getBlockX() == playerLocation.getBlockX() &&
                    checkpointLocation.getBlockY() == playerLocation.getBlockY() &&
                    checkpointLocation.getBlockZ() == playerLocation.getBlockZ()) {
                return true;
            }
        }
        return false;
    }
}