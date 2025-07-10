package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.parkour.progress.ProgressTrackerManager;
import org.astral.parkour_plugin.timer.IndividualTimerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class ParkourListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        ParkourManager.autoReconnectPlayersIfNecessary(player);
    }

    @EventHandler
    public void onPlayerMove(final @NotNull PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player);
        if (!playerInMap.isPresent()) return;

        final String name_map = playerInMap.get();
        final Location location = player.getLocation();
        if (ParkourManager.getModePlayer(player) == Mode.GLOBAL && !ParkourManager.canMove(name_map)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;
            final Location spawn = ParkourManager.getSpawnPlayer(player);
            ParkourManager.teleportToSpawnOrWarn(player, name_map, spawn);
        }

        double percent = ProgressTrackerManager.getNearestEndPointProgress(
                ParkourManager.getSpawnPlayer(player),
                ParkourManager.getFinishPoints(name_map),
                location
        );
        //System.out.println(percent);
        ParkourManager.saveCheckpointIfReached(player, name_map, location);
        ParkourManager.teleportIf(player, name_map, location);

    }

    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player);
        if (!playerInMap.isPresent()) return;
        final String name_map = playerInMap.get();
        if (!ParkourManager.isAutoReconnect(name_map)) {
            ParkourManager.removePlayerParkour(player);
        } else {
            if (ParkourManager.getModePlayer(player) == Mode.INDIVIDUAL) {
                IndividualTimerManager.pause(player);
            }
        }
    }

}
