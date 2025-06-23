package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.actiobar.ActionBar;
import org.astral.parkour_plugin.compatibilizer.adapters.TeleportingApi;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.parkour.action.TimerActionBar;
import org.astral.parkour_plugin.parkour.checkpoints.CheckpointBase;
import org.astral.parkour_plugin.parkour.progress.ProgressTracker;
import org.astral.parkour_plugin.parkour.progress.ProgressTrackerManager;
import org.astral.parkour_plugin.timer.GlobalTimerManager;
import org.astral.parkour_plugin.timer.IndividualTimerManager;
import org.astral.parkour_plugin.timer.Timer;
import org.astral.parkour_plugin.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class ParkourManager {

    private static final Main plugin = Main.getInstance();

    private static final Map<Player, ParkourPlayerData> playersInParkour = new HashMap<>();
    private static final Listener parkourListener = new ParkourListener();
    private static boolean activeListener = false;

    public static void registerOrUnregisterListener() {
        boolean hasPlayers = !playersInParkour.isEmpty();
        if (hasPlayers && !activeListener) {
            plugin.getServer().getPluginManager().registerEvents(parkourListener, plugin);
            activeListener = true;
        } else if (!hasPlayers && activeListener) {
            HandlerList.unregisterAll(parkourListener);
            activeListener = false;
        }
    }

    public static List<String> getAllPlayerNamesInParkour() {
        return playersInParkour.keySet().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    public static void starParkourHere(final @NotNull Player player, final String map) {
        final Location blockLocation = player.getLocation().clone().subtract(0, 1, 0).getBlock().getLocation();
        CheckpointBase.loadMap(map);
        addAndSave(player, blockLocation, map);
        final Rules rules = new Rules(map);
        Optional<Title> optionalTitle = rules.getStartTitle();
        optionalTitle.ifPresent(title -> title.send(player));
        rules.getMessage("start", player.getName()).ifPresent(player::sendMessage);
        if (rules.isTimerEnabled()){
            if (rules.isGlobalModeTime()){
                TimerActionBar.starIndividualTimer(rules, player, rules.isActionBarTimerDisplayEnabled());
            }else {
                TimerActionBar.startGlobalTimer(rules, player, rules.isActionBarTimerDisplayEnabled());
            }
        }
    }

    public static void gotoParkour(final Player player, final String map) {
        final Optional<Location> spawn = getRandomSpawn(map);
        if (!spawn.isPresent()) {
            player.sendMessage("§cNo existe ningún lugar de aparición definido para el mapa §b" + map + "§c.");
            return;
        }
        CheckpointBase.loadMap(map);
        addAndSave(player, spawn.get(), map);
        TeleportingApi.teleport(player, spawn.get());
        final Rules rules = new Rules(map);
        Optional<Title> optionalTitle = rules.getStartTitle();
        optionalTitle.ifPresent(title -> title.send(player));
        rules.getMessage("start", player.getName()).ifPresent(player::sendMessage);
        if (rules.isTimerEnabled()){
            if (rules.isGlobalModeTime()){
                TimerActionBar.starIndividualTimer(rules, player, rules.isActionBarTimerDisplayEnabled());
            }else {
                TimerActionBar.startGlobalTimer(rules, player, rules.isActionBarTimerDisplayEnabled());
            }
        }
    }

    public static void finish(final Player player) {
        ParkourPlayerData data = playersInParkour.get(player);
        if (data == null) return;

        String map = data.getMapName();
        final Timer timer = getTimer(player);
        boolean hasValidTime = timer != null;

        final String formattedTime = hasValidTime ? timer.getFormattedTime() : "";
        final String msg = "§a¡Buen trabajo! Completaste el parkour §b" + map +
                (hasValidTime ? " §aen §e" + formattedTime + "§a." : "§a.");
        player.sendMessage(msg);

        if (hasValidTime) {
            new ActionBar(formattedTime).send(player);
        }
        removePlayerParkour(player);
    }

    public static @Nullable Timer getTimer(final Player player) {
        ParkourPlayerData data = playersInParkour.get(player);
        if (data == null) return null;

        String map = data.getMapName();

        if (IndividualTimerManager.isRunning(player)) {
            return IndividualTimerManager.get(player);
        } else if (GlobalTimerManager.isRunning(map)) {
            return GlobalTimerManager.get(map);
        }

        return null;
    }

    public static void removePlayerParkour(final Player player) {
        final ParkourPlayerData data = playersInParkour.remove(player);
        if (data == null) return;
        final String map = data.getMapName();
        final ProgressTracker tracker = ProgressTrackerManager.get(map);
        tracker.removePlayer(player);
        if (tracker.getSortedByProgress(Collections.emptyList()).isEmpty()) {
            ProgressTrackerManager.remove(map);
        }
        IndividualTimerManager.stop(player);
        GlobalTimerManager.removeViewer(player);
        GlobalTimerManager.getViewingMap(player).ifPresent(viewingMap -> {
            if (GlobalTimerManager.getViewersOf(viewingMap).isEmpty()) {
                GlobalTimerManager.stop(viewingMap);
            }
        });
        registerOrUnregisterListener();
    }

    public static void addAndSave(final Player player, final Location location, final String map){
        playersInParkour.put(player, new ParkourPlayerData(map, location));
        registerOrUnregisterListener();
    }

    public static @NotNull List<Location> getFinishPoints(final String map){
        return new Rules(map).getEndPoints();
    }

    public static Optional<Location> getRandomSpawn(final String map) {
        final Rules rules = new Rules(map);
        final List<Location> spawnPoints = rules.getSpawnsPoints();

        if (spawnPoints.isEmpty()) return Optional.empty();

        final Location random = spawnPoints.get(new Random().nextInt(spawnPoints.size())).clone();
        random.add(0, 1, 0);
        return Optional.of(random);
    }

    public static Optional<String> getMapIfInParkour(final Player player) {
        ParkourPlayerData data = playersInParkour.get(player);
        return data != null ? Optional.of(data.getMapName()) : Optional.empty();
    }

    public static @Nullable Location getSpawnPlayer(final Player player){
        ParkourPlayerData data = playersInParkour.get(player);
        return data != null ? data.getSpawnLocation() : null;
    }



    public static boolean isAutoReconnect(final String map){
        final Rules rules = new Rules(map);
        return rules.isAutoReconnectEnabled();
    }
}