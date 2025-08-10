package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.parkour.Type.Type;
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
        System.out.println("move");
        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player.getUniqueId());
        if (!playerInMap.isPresent()) return;
        final String name_map = playerInMap.get();
        final Location location = player.getLocation();
        Type type = ParkourManager.getTypePlayer(player, name_map);
        boolean canMove = true;
        if (type == Type.GLOBAL){
            canMove = ParkourManager.canMoveGlobal(name_map);
        }else if(type == Type.INDIVIDUAL){
            canMove = ParkourManager.canMoveIndividual(player.getUniqueId());
        }
        if (!canMove) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;
            final Location spawn = ParkourManager.getSpawnPlayer(player.getUniqueId());
            ParkourManager.teleportToSpawnOrWarn(player, name_map, spawn);
            return;
        }
        double percent = ProgressTrackerManager.getMaxRadialProgress(
                ParkourManager.getSpawnPlayer(player.getUniqueId()),
                ParkourManager.getFinishPoints(name_map),
                location);

        System.out.println(percent);

        ParkourManager.saveCheckpointIfReached(player, name_map, location);
        ParkourManager.teleportIf(player, name_map, location);
        ParkourManager.endParkourIfNecessary(player, name_map, location);
    }

    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        System.out.println(player);
        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player.getUniqueId());
        if (!playerInMap.isPresent()) return;
        final String name_map = playerInMap.get();
        if (!ParkourManager.isAutoReconnect(name_map)) {
            ParkourManager.removePlayerParkour(player.getUniqueId());
        } else {
            Type type = ParkourManager.getTypePlayer(player, name_map);
            ParkourManager.hideMap(player, name_map);
            if (type == Type.INDIVIDUAL) {
                IndividualTimerManager.pause(player.getUniqueId());
            }
        }
    }

}
