package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.compatibilizer.adapters.TeleportingApi;
import org.astral.parkour_plugin.parkour.Type.Type;
import org.astral.parkour_plugin.parkour.progress.ProgressTrackerManager;
import org.astral.parkour_plugin.timer.GlobalTimerManager;
import org.astral.parkour_plugin.timer.IndividualTimerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ParkourListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        ParkourManager.autoReconnectPlayersIfNecessary(player);
    }

    @EventHandler
    public void onPlayerMove(final @NotNull PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player.getUniqueId());
        if (!playerInMap.isPresent()) return;
        final String name_map = playerInMap.get();
        if (IndividualTimerManager.isInPause(player.getUniqueId()) || GlobalTimerManager.isInPause(name_map)) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                    event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                    event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                Location fixed = event.getFrom().clone();
                fixed.setYaw(event.getTo().getYaw());
                fixed.setPitch(event.getTo().getPitch());
                TeleportingApi.teleport(player, fixed);
            }
            return;
        }
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

        List<String> sortedPlayers = ProgressTrackerManager.getSortedPlayersByRadialProgress(
                ParkourManager.getMapStateGlobal(name_map).getAllPlayers().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()),
                ParkourManager.getSpawnPlayer(player.getUniqueId()),
                ParkourManager.getFinishPoints(name_map)
        );

        System.out.println("Ranking en " + name_map + ": " + sortedPlayers);


        ParkourManager.saveCheckpointIfReached(player.getUniqueId(), name_map, location, type);
        ParkourManager.teleportIf(player, name_map, location);
        ParkourManager.endParkourIfNecessary(player, name_map, location);
    }

    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
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

    @EventHandler
    public void onPlayerInteract(final @NotNull PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player.getUniqueId());
        if (!playerInMap.isPresent()) return;
        ItemStack item = event.getItem();
        if (item != null){

        }
    }

}
