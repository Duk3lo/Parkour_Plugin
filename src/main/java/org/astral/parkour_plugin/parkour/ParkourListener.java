package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.compatibilizer.adapters.TeleportingApi;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.parkour.action.TimerActionBar;
import org.astral.parkour_plugin.parkour.checkpoints.Checkpoint;
import org.astral.parkour_plugin.parkour.checkpoints.CheckpointBase;
import org.astral.parkour_plugin.parkour.progress.ProgressTracker;
import org.astral.parkour_plugin.parkour.progress.ProgressTrackerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public final class ParkourListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player);
        if (!playerInMap.isPresent()) return;
        final String name_map = playerInMap.get();
        if (ParkourManager.isAutoReconnect(name_map)) {
            final Checkpoint checkpoint = CheckpointBase.getLastCheckpointPlayer(player);
            if (checkpoint != null) {
                teleportToCheckpoint(player, checkpoint);
            } else {
                final Location spawn = ParkourManager.getSpawnPlayer(player);
                teleportToSpawnOrWarn(player, name_map, spawn);
            }
            final Rules rules = new Rules(name_map);
            TimerActionBar.starIndividualTimer(rules, player);
        }
    }

    @EventHandler
    public void onPlayerMove(final @NotNull PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player);
        if (!playerInMap.isPresent()) return;

        final String name_map = playerInMap.get();
        final Location location = player.getLocation();
        if (ParkourManager.getModePlayer(player) == Mode.GLOBAL && !ParkourManager.canMove(name_map)) {
            final Location spawn = ParkourManager.getSpawnPlayer(player);
            teleportToSpawnOrWarn(player, name_map, spawn);
            return;
        }

        double percent = ProgressTrackerManager.getNearestEndPointProgress(
                ParkourManager.getSpawnPlayer(player),
                ParkourManager.getFinishPoints(name_map),
                location
        );

        saveCheckpointIfReached(player, name_map, location);
        teleportIf(player, name_map, location);
    }

    public void saveCheckpointIfReached(final Player player, final String name_map, final Location location){
        final List<Checkpoint> checkpoints = CheckpointBase.getCheckpoints(name_map);
        if (checkpoints == null || checkpoints.isEmpty()) return;

        for (int i = 0; i < checkpoints.size(); i++) {
            final Checkpoint checkpoint = checkpoints.get(i);
            final Location chekLoc = checkpoint.getLocation();
            if (CheckpointBase.isEqualLocation(chekLoc, location)) {
                if (checkpoint.getPlayers().contains(player)) return;
                checkpoint.getPlayers().add(player);
                CheckpointBase.addPlayerLastCheckpoint(player, checkpoint);
                ProgressTracker tracker = ProgressTrackerManager.get(name_map);
                tracker.updateCheckpoint(player, i);
                double progress = tracker.getProgress(player, checkpoints);
                player.sendActionBar("§bProgreso: §a" + String.format("%.2f", progress) + "§f%");
                return;
            }
        }
    }

    public void teleportIf(final Player player, final String name_map, final @NotNull Location location) {
        final double currentY = location.getY();
        final Checkpoint checkpoint = CheckpointBase.getLastCheckpointPlayer(player);
        if (checkpoint != null && (currentY < checkpoint.getMinY() || currentY > checkpoint.getMaxY())) {
            teleportToCheckpoint(player, checkpoint);
            return;
        }
        final double minY = CheckpointBase.getMinGeneralY(name_map, location.getWorld());
        final double maxY = CheckpointBase.getMaxGeneralY(name_map, location.getWorld());
        if (currentY >= minY && currentY <= maxY) return;
        if (checkpoint != null) {
            teleportToCheckpoint(player, checkpoint);
            return;
        }
        final Location spawn = ParkourManager.getSpawnPlayer(player);
        teleportToSpawnOrWarn(player, name_map, spawn);
    }

    private void teleportToCheckpoint(final @NotNull Player player, final @NotNull Checkpoint checkpoint) {
        Location checkpointLocation = checkpoint.getLocation().clone();
        Location playerLocation = player.getLocation();
        checkpointLocation.setYaw(playerLocation.getYaw());
        checkpointLocation.setPitch(playerLocation.getPitch());

        TeleportingApi.teleport(player, checkpointLocation);
    }

    private void teleportToSpawnOrWarn(final Player player, final String nameMap, final Location spawn) {
        if (spawn != null) {
            Location spawnWithDirection = spawn.clone();
            Location playerLocation = player.getLocation();
            spawnWithDirection.setYaw(playerLocation.getYaw());
            spawnWithDirection.setPitch(playerLocation.getPitch());
            TeleportingApi.teleport(player, spawnWithDirection);
        } else {
            player.sendMessage("§cNo se pudo encontrar ningún punto de aparición para el mapa §b" + nameMap + "§c.");
        }
    }

    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final Optional<String> playerInMap = ParkourManager.getMapIfInParkour(player);
        if (!playerInMap.isPresent()) return;
        final String name_map = playerInMap.get();
        if (!ParkourManager.isAutoReconnect(name_map) || !ParkourManager.isInGame(name_map)) {
            ParkourManager.removePlayerParkour(player);
        }
    }

}
