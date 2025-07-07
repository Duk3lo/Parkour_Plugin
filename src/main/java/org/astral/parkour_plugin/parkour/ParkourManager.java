package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.actiobar.ActionBar;
import org.astral.parkour_plugin.compatibilizer.adapters.SoundApi;
import org.astral.parkour_plugin.compatibilizer.adapters.TeleportingApi;
import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.config.maps.title.RichText;
import org.astral.parkour_plugin.parkour.action.TimerActionBar;
import org.astral.parkour_plugin.parkour.checkpoints.CheckpointBase;
import org.astral.parkour_plugin.parkour.progress.ProgressTracker;
import org.astral.parkour_plugin.parkour.progress.ProgressTrackerManager;
import org.astral.parkour_plugin.timer.GlobalTimerManager;
import org.astral.parkour_plugin.timer.IndividualTimerManager;
import org.astral.parkour_plugin.timer.Timer;
import org.astral.parkour_plugin.title.Title;
import org.astral.parkour_plugin.views.Type;
import org.astral.parkour_plugin.views.tag_name.ArmorStandApi;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class ParkourManager {

    private static final Main plugin = Main.getInstance();

    private static final Map<UUID, ParkourPlayerData> playersInParkour = new HashMap<>();
    private static final Map<String, ParkourMapState> parkourMapStates = new ConcurrentHashMap<>();
    private static final Map<String, WaitingLobbyState> activeWaitingLobbies = new ConcurrentHashMap<>();

    private static ScheduledTask waitingTask;
    private static final Listener parkourListener = new ParkourListener();
    private static boolean activeListener = false;
    private static final ArmorStandApi hologram = plugin.getArmorStandApi();

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
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    public static void starParkourHere(final @NotNull Player player, final String map) {
        final Location blockLocation = player.getLocation().clone().subtract(0, 1, 0).getBlock().getLocation();
        CheckpointBase.loadMap(map);
        addAndSave(player.getUniqueId(), blockLocation, map, Mode.INDIVIDUAL);
        final Rules rules = new Rules(map);
        final Optional<RichText> optionalTitle = rules.getTitle("start");
        optionalTitle.ifPresent(title ->
                new Title(title.getTitle(), title.getSubtitle(), title.getFadeIn(), title.getStay(), title.getFadeOut()).send(player));
        rules.getMessage("start", player.getName()).ifPresent(player::sendMessage);
        if (rules.isIndividualTimerEnabled()){
            TimerActionBar.starIndividualTimer(rules, player);
        }
        showAllObjectsInMap(player, map);
    }

    public static void gotoParkour(final Player player, final String map) {

        final Optional<Location> spawn = getRandomSpawn(map);
        if (!spawn.isPresent()) {
            player.sendMessage("§cNo existe ningún lugar de aparición definido para el mapa §b" + map + "§c.");
            return;
        }
        CheckpointBase.loadMap(map);
        addAndSave(player.getUniqueId(), spawn.get(), map, Mode.GLOBAL);
        TeleportingApi.teleport(player, spawn.get());
        final Rules rules = new Rules(map);
        final Optional<RichText> optionalTitle = rules.getTitle("start");
        optionalTitle.ifPresent(title -> new Title(title.getTitle(), title.getSubtitle(), title.getFadeIn(), title.getStay(), title.getFadeOut()).send(player));
        rules.getMessage("start", player.getName()).ifPresent(player::sendMessage);

        showAllObjectsInMap(player, map);
        if (rules.isWaitingLobbyEnabled()) {
            parkourMapStates.computeIfAbsent(map, k -> new ParkourMapState());
            parkourMapStates.get(map).setCanMove(rules.isWaitingLobbyMovementAllowed());
            activeWaitingLobbies.put(map, new WaitingLobbyState(rules));
            startWaitingSchedulerIfNeeded();
        }
    }

    private static void startWaitingSchedulerIfNeeded() {
        if (waitingTask != null && !waitingTask.isCancelled()) return;
        waitingTask = Kit.getAsyncScheduler().runAtFixedRate(plugin, t -> {
            if (activeWaitingLobbies.isEmpty()) {
                t.cancel();
                return;
            }

            for (Map.Entry<String, WaitingLobbyState> entry : activeWaitingLobbies.entrySet()) {
                String map = entry.getKey();
                WaitingLobbyState state = entry.getValue();
                Rules rules = state.getRules();


                int current = countPlayersInMap(map);
                int min = rules.getWaitingLobbyMinPlayers();
                int maxTime = rules.getWaitingLobbyMaxWaitTimeSeconds();

                if (maxTime > -1 && state.incrementTimer() >= maxTime) {
                    loadFramesIfNeedAndRemove(state);
                    continue;
                }
                if (current >= min) {
                    loadFramesIfNeedAndRemove(state);
                    continue;
                }

                if (rules.isWaitingLobbyActionBarEnabled()) {
                    String format = rules.getWaitingLobbyFormat()
                            .replace("{current}", String.valueOf(current))
                            .replace("{required}", String.valueOf(min))
                            .replace("{dots}", state.getAnimatedDots());
                    getOnlinePlayersInMap(map).forEach(p -> new ActionBar(format).send(p));
                }
            }
        }, 0L, 1L, TimeUnit.SECONDS);
    }

    private static void loadFramesIfNeedAndRemove(@NotNull WaitingLobbyState state) {
        if (state.getCountDownTask() == null) {
            final Rules rules = state.getRules();
            String map = rules.getMapName();
            Set<Player> players = getOnlinePlayersInMap(map);
            parkourMapStates.get(map).setInGame(true);

            for (Player player : players){
                Location location = playersInParkour.get(player.getUniqueId()).getSpawnLocation();
                TeleportingApi.teleport(player, location);
            }

            int delay = state.getAnimatedTimerPreStar();
            if (delay == 0) {
                activeWaitingLobbies.remove(map);
            } else {
                ScheduledTask countDown = Kit.getAsyncScheduler().runAtFixedRate(plugin, s -> rules.getAnimatedTitle("star_countdown").ifPresent(animatedRichText -> {
                    int index = state.getStart();
                    int framesSize = animatedRichText.getFrames().size();
                    if (index >= framesSize) {
                        state.setCountDownTask(null);
                        activeWaitingLobbies.remove(map);
                        s.cancel();
                        return;
                    }

                    RichText richText = animatedRichText.getFrames().get(index);
                    boolean isLastFrame = index == framesSize - 1;
                    for (Player player : players) {
                        if (isLastFrame) {
                            parkourMapStates.get(map).setCanMove(true);
                            SoundApi.playSound(player, 1.0f, 1.0f, "FIREWORK_BLAST", "ENTITY_FIREWORK_ROCKET_BLAST");
                            if (rules.isGlobalTimerEnabled()){
                                TimerActionBar.startGlobalTimer(rules, player);
                            }
                        } else {
                            SoundApi.playSound(player, 1.0f, 1.0f, "NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING");
                        }
                        new Title(
                                richText.getTitle(),
                                richText.getSubtitle(),
                                richText.getFadeIn(),
                                richText.getStay(),
                                richText.getFadeOut()
                        ).send(player);
                    }
                    state.incrementStart();
                }), 0, delay, TimeUnit.SECONDS);

                state.setCountDownTask(countDown);
            }
        }
    }

    private static int countPlayersInMap(final String mapName) {
        return (int) playersInParkour.entrySet().stream()
                .filter(entry -> entry.getValue().getMapName().equalsIgnoreCase(mapName))
                .count();
    }

    static boolean isInGame(final String map){
        return parkourMapStates.get(map).isInGame();
    }

    private static Set<Player> getOnlinePlayersInMap(final String mapName) {
        return playersInParkour.entrySet().stream()
                .filter(entry -> entry.getValue().getMapName().equalsIgnoreCase(mapName))
                .map(entry -> Bukkit.getPlayer(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static void showAllObjectsInMap(final Player player, final String map){
        for (Type type : Type.values()){
            hologram.showHolograms(player, map, type);
        }
    }

    public static void hideMap(final Player player, final String map){
        for (Type type : Type.values()){
            hologram.hideHolograms(player, map, type);
        }
    }

    public static void finish(final @NotNull Player player) {
        ParkourPlayerData data = playersInParkour.get(player.getUniqueId());
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

    public static @Nullable Timer getTimer(final @NotNull Player player) {
        ParkourPlayerData data = playersInParkour.get(player.getUniqueId());
        if (data == null) return null;

        String map = data.getMapName();

        if (IndividualTimerManager.isRunning(player)) {
            return IndividualTimerManager.get(player);
        } else if (GlobalTimerManager.isRunning(map)) {
            return GlobalTimerManager.get(map);
        }

        return null;
    }

    public static void removePlayerParkour(final @NotNull Player player) {
        final ParkourPlayerData data = playersInParkour.remove(player.getUniqueId());
        if (data == null) return;
        final String map = data.getMapName();
        hideMap(player, map);
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

    public static void addAndSave(final UUID uuid, final Location location, final String map, Mode mode){
        playersInParkour.put(uuid, new ParkourPlayerData(map, location, mode));
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

    public static Optional<String> getMapIfInParkour(final @NotNull Player player) {
        ParkourPlayerData data = playersInParkour.get(player.getUniqueId());
        return data != null ? Optional.of(data.getMapName()) : Optional.empty();
    }

    public static @Nullable Location getSpawnPlayer(final @NotNull Player player){
        ParkourPlayerData data = playersInParkour.get(player.getUniqueId());
        return data != null ? data.getSpawnLocation() : null;
    }

    public static boolean isAutoReconnect(final String map){
        final Rules rules = new Rules(map);
        return rules.isAutoReconnectEnabled();
    }

    public static boolean canMove(final String map) {
        ParkourMapState state = parkourMapStates.get(map);
        if (state == null) {
            return true;
        }
        return state.canMove();
    }

    public static Mode getModePlayer(final @NotNull Player player){
        final UUID uuid = player.getUniqueId();
        return playersInParkour.get(uuid).getMode();
    }
}