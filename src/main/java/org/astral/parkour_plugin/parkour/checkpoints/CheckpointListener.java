package org.astral.parkour_plugin.parkour.checkpoints;

import org.astral.parkour_plugin.compatibilizer.adapters.TeleportingApi;
import org.astral.parkour_plugin.parkour.ParkourManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public final class CheckpointListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event){
        final Player player = event.getPlayer();
        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player);
        if (playerInMap.isPresent()){

        }
    }

    @EventHandler
    public void onPlayerMove(final @NotNull PlayerMoveEvent event){
        final Player player = event.getPlayer();
        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player);
        if (playerInMap.isPresent()){
            final Location location = player.getLocation();
            final String name_map = playerInMap.get();
            saveCheckpointIfReached(player, name_map, location);
            teleportIf(player, name_map, location);
        }
    }

    public void saveCheckpointIfReached(final Player player, final String name_map, final Location location){
        final Set<Checkpoint> checkpoints = CheckpointBase.getCheckpoints(name_map);
        if (checkpoints == null || checkpoints.isEmpty()) return;
        for (final Checkpoint checkpoint : checkpoints){
            final Location chekLoc = checkpoint.getLocation();
            if (CheckpointBase.isEqualLocation(chekLoc, location)){
                if (checkpoint.getPlayers().contains(player)) continue;
                checkpoint.getPlayers().add(player);
                CheckpointBase.addPlayerLastCheckpoint(player, checkpoint);
            }
        }
    }

    public void teleportIf(final Player player, final String name_map, final @NotNull Location location) {
        final double currentY = location.getY();
        final Checkpoint checkpoint = CheckpointBase.getLastCheckpointPlayer(player);
        if (checkpoint != null && currentY < checkpoint.getMinY()) {
            TeleportingApi.teleport(player, checkpoint.getLocation());
            return;
        }
        final double minY = CheckpointBase.getMinGeneralY(name_map, location.getWorld());
        final double maxY = CheckpointBase.getMaxGeneralY(name_map, location.getWorld());
        if (currentY >= minY && currentY <= maxY) return;
        if (checkpoint != null) {
            TeleportingApi.teleport(player, checkpoint.getLocation());
            return;
        }
        final Location spawn = ParkourManager.getSpawnPlayer(player);
        if (spawn != null) {
            TeleportingApi.teleport(player, spawn);
        } else {
            player.sendMessage("§cNo se pudo encontrar ningún punto de aparición para el mapa §b" + name_map + "§c.");
        }
    }


    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {

    }

}
