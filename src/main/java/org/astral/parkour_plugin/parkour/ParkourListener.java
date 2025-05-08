package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.compatibilizer.adapters.TeleportingApi;
import org.astral.parkour_plugin.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ParkourListener implements Listener {

    //Instances
    private final static Main Plugin = Main.getInstance();
    private final Parkour parkour = Plugin.getParkour();

    //Maps
    private final Map<String, List<Checkpoint>> checkpointMap = parkour.getCheckpointsMap();
    private final Map<Player, Checkpoint> playerLastCheckpoint = parkour.getPlayerLastCheckpoint();

    //Set
    private final Set<Player> players = parkour.getPlayerOnParkour();

    @EventHandler
    public void onPlayerMove(final @NotNull PlayerMoveEvent event){
        final Player player = event.getPlayer();
        if (players.contains(player)){
            final Location location = player.getLocation();
            final String MapName = parkour.getMapNameByPlayer(player);
            final List<Checkpoint> checkpoints = checkpointMap.get(MapName);
            if (checkpoints != null) {
                if (hasPlayerPositionCorrect(location, player)){
                    final Checkpoint lastCheckpoint = playerLastCheckpoint.get(player);
                    if (lastCheckpoint != null){
                        final Location lastLocation = getLocation(lastCheckpoint, player);
                        TeleportingApi.teleport(player, location);
                    }
                }
                for (final Checkpoint checkpoint : checkpoints) {
                    if (checkpoint.isArea()) {
                        if (Parkour.isLocationMatch(Objects.requireNonNull(checkpoint.getLocations()), location)) {
                            checkpoint.getPlayers().computeIfAbsent(MapName, V -> new HashSet<>()).add(player);
                            playerLastCheckpoint.put(player, checkpoint);
                            return;
                        }
                    } else {
                        if (Parkour.isLocationMatch(Objects.requireNonNull(checkpoint.getLocation()), location)) {
                            checkpoint.getPlayers().computeIfAbsent(MapName, V -> new HashSet<>()).add(player);
                            playerLastCheckpoint.put(player, checkpoint);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (players.contains(player)) {
            final String mapName = parkour.getMapNameByPlayer(player);
            if (!mapName.isEmpty()) {
                if (parkour.removePlayerParkourMap(mapName, player)) {
                    Plugin.getLogger().info(player.getName() + " se ha retirado del parkour en el mapa " + mapName);
                }
            }
        }
    }

    private @NotNull Location getLocation(final @NotNull Checkpoint lastCheckpoint, final @NotNull Player player) {
        final Location lastLocation = lastCheckpoint.isArea() ? lastCheckpoint.centerLocationOfXZ() : lastCheckpoint.getLocation();
        assert lastLocation != null;
        lastLocation.setX(lastLocation.getBlockX() + 0.5);
        lastLocation.setY(lastLocation.getBlockY() + 0.5);
        lastLocation.setZ(lastLocation.getBlockZ() + 0.5);
        final float yaw = player.getLocation().getYaw();
        final float pitch = player.getLocation().getPitch();
        lastLocation.setYaw(yaw);
        lastLocation.setPitch(pitch);
        parkour.addPlayerOfLastCheckpoint(player, lastLocation);
        return lastLocation;
    }

    private boolean hasPlayerPositionCorrect(final Location location, final Player player) {
        final Checkpoint lastCheckpoint = playerLastCheckpoint.get(player);
        if (lastCheckpoint != null) {
            return location.getY() <= lastCheckpoint.getMinFallY() || location.getY() >= lastCheckpoint.getMaxFallY();
        }
        return false;
    }
}