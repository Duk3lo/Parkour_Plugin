package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.actiobar.ActionBar;
import org.astral.parkour_plugin.compatibilizer.adapters.SoundApi;
import org.astral.parkour_plugin.compatibilizer.adapters.TeleportingApi;
import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.config.maps.title.AnimatedRichText;
import org.astral.parkour_plugin.config.maps.title.RichText;
import org.astral.parkour_plugin.gui.Gui;
import org.astral.parkour_plugin.parkour.Type.ParkourMapStateGlobal;
import org.astral.parkour_plugin.parkour.Type.ParkourMapStateIndividual;
import org.astral.parkour_plugin.parkour.Type.Type;
import org.astral.parkour_plugin.parkour.action.TimerActionBar;
import org.astral.parkour_plugin.parkour.checkpoints.Checkpoint;
import org.astral.parkour_plugin.parkour.checkpoints.CheckpointBase;
import org.astral.parkour_plugin.parkour.progress.ProgressTracker;
import org.astral.parkour_plugin.parkour.progress.ProgressTrackerManager;
import org.astral.parkour_plugin.parkour.titles.MapAnimationKey;
import org.astral.parkour_plugin.timer.GlobalTimerManager;
import org.astral.parkour_plugin.timer.IndividualTimerManager;
import org.astral.parkour_plugin.timer.Timer;
import org.astral.parkour_plugin.title.Title;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class ParkourManager {

    private static final Main plugin = Main.getInstance();

    private static final Map<String, ParkourMapStateGlobal> parkourMapStatesGlobal = new ConcurrentHashMap<>();
    private static final Map<UUID, ParkourMapStateIndividual> parkourMapStateIndividual = new HashMap<>();
    private static final Map<MapAnimationKey, ScheduledTask> taskAnimation = new ConcurrentHashMap<>();

    private static ScheduledTask waitingTask;

    private static final Listener parkourListener = new ParkourListener();
    private static boolean activeListener = false;
    private static final ArmorStandApi hologram = plugin.getArmorStandApi();

    public static void registerOrUnregisterListener() {
        boolean hasPlayersGlobal = parkourMapStatesGlobal.values().stream()
                .anyMatch(state -> !state.getAllPlayers().isEmpty());
        boolean hasPlayersIndividual = !parkourMapStateIndividual.isEmpty();

        boolean hasPlayers = hasPlayersGlobal || hasPlayersIndividual;

        if (hasPlayers) {
            if (!activeListener) {
                plugin.getServer().getPluginManager().registerEvents(parkourListener, plugin);
                activeListener = true;
            }
        } else {
            if (activeListener) {
                HandlerList.unregisterAll(parkourListener);
                activeListener = false;
            }
        }
    }

    public static ParkourMapStateGlobal getMapStateGlobal(String name) {
        return parkourMapStatesGlobal.get(name);
    }

    public static ParkourMapStateIndividual getMapStateIndividual(UUID uuid){
        return parkourMapStateIndividual.get(uuid);
    }

    public static @NotNull Set<UUID> getAllPlayers() {
        Set<UUID> allPlayers = parkourMapStatesGlobal.values().stream()
                .map(ParkourMapStateGlobal::getAllPlayers)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        allPlayers.addAll(parkourMapStateIndividual.keySet());
        return allPlayers;
    }

    public static List<String> getAllPlayerNamesInParkour() {
        return parkourMapStatesGlobal.values().stream()
                .flatMap(state -> state.getAllPlayers().stream())
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    public static void starParkourIndividual(final @NotNull Player player, final String map) {
        final UUID uuid = player.getUniqueId();
        ParkourMapStateIndividual state = parkourMapStateIndividual.computeIfAbsent(uuid, k->new ParkourMapStateIndividual(map));
        registerOrUnregisterListener();
        final Location blockLocation = player.getLocation(); //.clone().subtract(0, 1, 0).getBlock().getLocation();
        CheckpointBase.loadMap(map);
        final Rules rules = new Rules(map);
        final Optional<RichText> optionalTitle = rules.getTitle("join");
        optionalTitle.ifPresent(title ->
                new Title(title.getTitle(), title.getSubtitle(), title.getFadeIn(), title.getStay(), title.getFadeOut()).send(player));
        rules.getMessage("join", player.getName()).ifPresent(player::sendMessage);
        rules.getAnimatedTitle("star_countdown").ifPresent(state::setAnimatedRichText);
        state.setData(new ParkourPlayerData(blockLocation));
        state.setDisplayActionBarTimer(rules.isIndividualActionBarTimerDisplayEnabled());
        state.setTimeLimit(rules.getIndividualTimeLimit());
        state.setFormat(rules.getIndividualTimerFormat());
        state.setCountdown(rules.isIndividualCountdownEnabled());
        state.setTimerEnable(rules.isIndividualTimerEnabled());
        boolean canMove = state.getAnimatedRichText().getFrames().size() <= 1;
        state.setCanMove(canMove);
        loadFrames(state.getAnimatedRichText(), Collections.singleton(uuid), map, Type.INDIVIDUAL);
        state.setInGame(true);
        showAllObjectsInMap(player, map);
    }

    public static void startParkourGlobal(@NotNull Player player, String map) {
        ParkourMapStateGlobal state = parkourMapStatesGlobal.get(map);
        if (state != null) {
            if (state.isInGame()) {
                player.sendMessage("§cEste modo ya está en juego, no te puedes unir.");
                return;
            }
            if (!state.hasUnlimitedPlayers() && state.isFull()) {
                player.sendMessage("§cEste modo ha alcanzado el número máximo de jugadores.");
                return;
            }
        }
        Location spawn = getRandomSpawn(map);
        if (spawn == null) {
            player.sendMessage("§cNo se pudo encontrar ningún punto de aparición para el mapa §b" + map + "§c.");
            return;
        }
        CheckpointBase.loadMap(map);
        teleportToSpawnOrWarn(player, map, spawn);
        Rules rules = new Rules(map);
        state = parkourMapStatesGlobal.computeIfAbsent(map, k -> {
            ParkourMapStateGlobal newState = new ParkourMapStateGlobal(
                    k,
                    rules.getWaitingLobbyMinPlayers(),
                    rules.getWaitingLobbyMaxPlayers()
            );
            newState.setDisplayActionBarTimer(rules.isGlobalActionBarTimerDisplayEnabled());
            newState.setCanMove(rules.isWaitingLobbyMovementAllowed());
            newState.setTimeLimit(rules.getGlobalTimeLimit());
            newState.setCountdown(rules.isGlobalCountdownEnabled());
            newState.setFormat(rules.getGlobalTimerFormat());
            return newState;
        });
        state.addPlayer(player.getUniqueId(), new ParkourPlayerData(spawn));
        registerOrUnregisterListener();
        rules.getTitle("join").ifPresent(title ->
                new Title(title.getTitle(), title.getSubtitle(),
                        title.getFadeIn(), title.getStay(), title.getFadeOut())
                        .send(player)
        );
        rules.getMessage("join", player.getName()).ifPresent(player::sendMessage);
        rules.getAnimatedTitle("star_countdown").ifPresent(state::setAnimatedRichText);
        if (rules.isWaitingLobbyEnabled()) {
            state.setWaitingPlayers(true);
            state.setLimitTimeWait(rules.getWaitingLobbyMaxWaitTimeSeconds());
            state.setDisplayWaitingPlayer(rules.isWaitingLobbyActionBarEnabled());
            state.setFormatWaiting(rules.getWaitingLobbyFormat());
            state.setTimerEnable(rules.isGlobalTimerEnabled());
            waitSchedulerGlobal();
        } else {
            state.setWaitingPlayers(false);
            loadFrames(state.getAnimatedRichText(), state.getAllPlayers(), map, Type.GLOBAL);
        }
        showAllObjectsInMap(player, map);
    }

    public static void autoReconnectPlayersIfNecessary(final @NotNull Player player){
        final Optional<String> playerInMap = getMapIfInParkour(player.getUniqueId());
        if (!playerInMap.isPresent()) return;
        final String name_map = playerInMap.get();
        if (isAutoReconnect(name_map)) {
            Type type = getTypePlayer(player, name_map);
            if (isInGameIndividual(player.getUniqueId())||isInGameGlobal(name_map)){
                final Checkpoint checkpoint = CheckpointBase.getLastCheckpointPlayer(player);
                if (checkpoint != null) {
                    teleportToCheckpoint(player, checkpoint);
                } else {
                    final Location spawn = getSpawnPlayer(player.getUniqueId());
                    teleportToSpawnOrWarn(player, name_map, spawn);
                }
                if (type == Type.INDIVIDUAL){
                    TimerActionBar.starIndividualTimer(parkourMapStateIndividual.get(player.getUniqueId()), player.getUniqueId());
                }
                showAllObjectsInMap(player, name_map);
            }
        }
    }

    public static void saveCheckpointIfReached(final Player player, final String name_map, final Location location) {
        final List<Checkpoint> checkpoints = CheckpointBase.getCheckpoints(name_map);
        if (checkpoints == null || checkpoints.isEmpty()) return;
        for (int i = 0; i < checkpoints.size(); i++) {
            final Checkpoint checkpoint = checkpoints.get(i);
            final Location checkpointLoc = checkpoint.getLocation();
            final Location finalCheckpoint = checkpointLoc.clone();
            finalCheckpoint.add(0,1,0);

            if (CheckpointBase.isEqualLocation(finalCheckpoint, location)) {
                if (checkpoint.getPlayers().contains(player.getUniqueId())) return;

                checkpoint.getPlayers().add(player.getUniqueId());
                CheckpointBase.addPlayerLastCheckpoint(player, checkpoint);

                ProgressTracker tracker = ProgressTrackerManager.get(name_map);
                tracker.updateCheckpoint(player, i);
                double progress = tracker.getProgress(player.getUniqueId(), checkpoints);
                double progressCompletion = tracker.getCheckpointCompletionPercentage(player.getUniqueId(), checkpoints);
                System.out.println(progress);
                System.out.println(progressCompletion);

                // player.sendActionBar("§bProgreso: §a" + String.format("%.2f", progress) + "§f%");
                return;
            }
        }
    }

    public static void endParkourIfNecessary(final Player player, final String name_map, final Location location) {
        final List<Location> finishPoints = getFinishPoints(name_map);
        if (finishPoints.isEmpty()) return;
        for (Location finishLoc : finishPoints) {
            final Location adjustedLoc = finishLoc.clone().add(0, 1, 0);
            if (CheckpointBase.isEqualLocation(adjustedLoc, location)) {
                finish(player);
                return;
            }
        }
    }

    public static void teleportIf(final Player player, final String name_map, final @NotNull Location location) {
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
        final Location spawn = getSpawnPlayer(player.getUniqueId());
        teleportToSpawnOrWarn(player, name_map, spawn);
    }

    public static void teleportToCheckpoint(final @NotNull Player player, final @NotNull Checkpoint checkpoint) {
        Location checkpointLocation = checkpoint.getLocation().clone();
        Location playerLocation = player.getLocation();
        checkpointLocation.setYaw(playerLocation.getYaw());
        checkpointLocation.setPitch(playerLocation.getPitch());
        checkpointLocation.add(0,1,0);
        TeleportingApi.teleport(player, checkpointLocation);
    }

    public static void teleportToSpawnOrWarn(final Player player, final String nameMap, final Location spawn) {
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



    public static void waitSchedulerGlobal(){
        if (waitingTask != null && !waitingTask.isCancelled()) return;
        waitingTask = Kit.getAsyncScheduler().runAtFixedRate(plugin, t -> {
            boolean noneWaiting = parkourMapStatesGlobal.values().stream()
                    .noneMatch(ParkourMapStateGlobal::isWaitingPlayers);

            if (noneWaiting) {
                t.cancel();
                return;
            }

            for (Map.Entry<String, ParkourMapStateGlobal> entry : parkourMapStatesGlobal.entrySet()) {
                String map = entry.getKey();
                ParkourMapStateGlobal mapGlobal = entry.getValue();
                int current = mapGlobal.getCurrentPlayers();
                int min_players = mapGlobal.getMinPlayers();
                int maxTime = mapGlobal.getLimitTimeWait();
                int elapsed = mapGlobal.incrementTimer();
                AnimatedRichText animatedRichText = mapGlobal.getAnimatedRichText();
                Set<UUID> uuids = mapGlobal.getAllPlayers();

                if (maxTime > 0) {
                    boolean shouldStartSoon = (elapsed >= maxTime * 0.75);
                    mapGlobal.setStartingSoon(shouldStartSoon);
                }

                if (!isInGameGlobal(map)) {
                    if ((maxTime > -1 && elapsed >= maxTime) || current >= min_players) {
                        mapGlobal.setWaitingPlayers(false);
                        mapGlobal.setCanMove(true);
                        mapGlobal.setInGame(true);
                        loadFrames(animatedRichText, uuids, map, Type.GLOBAL);
                        continue;
                    }
                    if (mapGlobal.isDisplayWaitingPlayer()) {
                        String format = mapGlobal.getFormatWaiting()
                                .replace("{current}", String.valueOf(current))
                                .replace("{required}", String.valueOf(min_players))
                                .replace("{dots}", mapGlobal.getAnimatedDots());
                        getOnlinePlayersInMap(map).forEach(p -> new ActionBar(format).send(p));
                    }
                }

            }
        },0L, 1L, TimeUnit.SECONDS);
    }

    public static void loadFrames(@NotNull AnimatedRichText textAnimated, Set<UUID> uuids, String map, Type type) {
        int framesSize = textAnimated.getFrames().size();
        if (framesSize == 0) {
            Kit.getAsyncScheduler().runDelayed(plugin, t -> {
                for (UUID uuid : uuids) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        SoundApi.playSound(player, 1.0f, 1.0f,
                                "FIREWORK_BLAST", "ENTITY_FIREWORK_ROCKET_BLAST");
                        if (type == Type.GLOBAL) {
                            TimerActionBar.startGlobalTimer(parkourMapStatesGlobal.get(map), uuid);
                            parkourMapStatesGlobal.get(map).setCanMove(true);
                        } else {
                            TimerActionBar.starIndividualTimer(parkourMapStateIndividual.get(uuid), uuid);
                            parkourMapStateIndividual.get(uuid).setCanMove(true);
                        }
                    }
                }
            }, 1, TimeUnit.SECONDS);

            return;
        }

        int delay = textAnimated.getUpdateDelaySeconds();
        final AtomicInteger index = new AtomicInteger(0);
        final MapAnimationKey key;
        if (type == Type.INDIVIDUAL) {
            UUID firstUuid = uuids.stream().findFirst().orElse(null);
            key = new MapAnimationKey(map, type, firstUuid);
        } else key = new MapAnimationKey(map, type);

        ScheduledTask taskFrames = Kit.getAsyncScheduler().runAtFixedRate(plugin, t -> {
            int i = index.getAndIncrement();
            if (i >= framesSize) {
                t.cancel();
                taskAnimation.remove(key);
                return;
            }

            RichText frame = textAnimated.getFrames().get(i);
            boolean isLastFrame = (i == framesSize - 1);

            for (UUID uuid : uuids) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    if (isLastFrame) {
                        SoundApi.playSound(player, 1.0f, 1.0f,
                                "FIREWORK_BLAST", "ENTITY_FIREWORK_ROCKET_BLAST");
                        if (type == Type.GLOBAL) {
                            TimerActionBar.startGlobalTimer(parkourMapStatesGlobal.get(map), uuid);
                            parkourMapStatesGlobal.get(map).setCanMove(true);
                        } else {
                            TimerActionBar.starIndividualTimer(parkourMapStateIndividual.get(uuid), uuid);
                            parkourMapStateIndividual.get(uuid).setCanMove(true);
                        }
                    } else {
                        SoundApi.playSound(player, 1.0f, 1.0f,
                                "NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING");
                    }
                    new Title(
                            frame.getTitle(),
                            frame.getSubtitle(),
                            frame.getFadeIn(),
                            frame.getStay(),
                            frame.getFadeOut()
                    ).send(player);
                }
            }
        }, 0, delay, TimeUnit.SECONDS);

        taskAnimation.put(key, taskFrames);
    }



    public static boolean isInGameGlobal(final String map) {
        ParkourMapStateGlobal state = parkourMapStatesGlobal.get(map);
        if (state != null) {
            return state.isInGame();
        }
        return false;
    }

    public static boolean isInGameIndividual(final UUID uuid) {
        ParkourMapStateIndividual stateIndividual = parkourMapStateIndividual.get(uuid);
        if (stateIndividual != null) {
            return stateIndividual.isInGame();
        }
        return false;
    }

    private static Set<Player> getOnlinePlayersInMap(final String mapName) {
        ParkourMapStateGlobal state = parkourMapStatesGlobal.get(mapName);
        if (state == null) return Collections.emptySet();

        return state.getPlayersMap().keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .collect(Collectors.toSet());
    }


    public static void showAllObjectsInMap(final Player player, final String map){
        for (org.astral.parkour_plugin.views.Type type : org.astral.parkour_plugin.views.Type.values()){
            hologram.showHolograms(player, map, type);
        }
    }

    public static void hideMap(final Player player, final String map){
        for (org.astral.parkour_plugin.views.Type type : org.astral.parkour_plugin.views.Type.values()){
            hologram.hideHolograms(player, map, type);
        }
    }

    public static void finish(final @NotNull Player player) {
        UUID uuid = player.getUniqueId();
        ParkourMapStateIndividual stateIndividual = parkourMapStateIndividual.get(uuid);
        ParkourMapStateGlobal stateGlobal = null;
        String mapName;
        boolean isIndividual = false;
        if (stateIndividual != null) {
            isIndividual = true;
            mapName = stateIndividual.getName();
        } else {
            stateGlobal = getPlayerCurrentMapState(uuid);
            if (stateGlobal == null) return;
            mapName = stateGlobal.getName();
        }
        ParkourPlayerData data = isIndividual
                ? stateIndividual.getData()
                : stateGlobal.getPlayersMap().get(uuid);
        if (data == null) return;
        final Timer timer = getTimer(player);
        boolean hasValidTime = timer != null;
        final String formattedTime = hasValidTime ? timer.getFormattedTime() : "";
        String msg;
        if (isIndividual) {
            msg = "§a¡Has completado tu parkour personal!";
            if (hasValidTime) {
                msg += " §aTiempo: §e" + formattedTime + "§a.";
            }
        } else {
            msg = "§a¡Buen trabajo! Completaste el parkour global §b" + mapName;
            if (hasValidTime) {
                msg += " §aen §e" + formattedTime + "§a.";
            }
        }
        player.sendMessage(msg);
        if (hasValidTime) {
            new ActionBar(formattedTime).send(player);
        }
        removePlayerParkour(uuid);
    }

    private static ParkourMapStateGlobal getPlayerCurrentMapState(UUID uuid) {
        return parkourMapStatesGlobal.values().stream()
                .filter(state -> state.getPlayersMap().containsKey(uuid))
                .findFirst()
                .orElse(null);
    }

    public static @Nullable Timer getTimer(final @NotNull Player player) {
        UUID uuid = player.getUniqueId();
        ParkourMapStateIndividual individualState = parkourMapStateIndividual.get(uuid);
        if (individualState != null && IndividualTimerManager.isRunning(uuid)) {
            return IndividualTimerManager.get(uuid);
        }
        ParkourMapStateGlobal globalState = getPlayerCurrentMapState(uuid);
        if (globalState != null) {
            String map = globalState.getName();
            if (GlobalTimerManager.isRunning(map)) {
                return GlobalTimerManager.get(map);
            }
        }
        return null;
    }

    public static void removePlayerParkour(final @NotNull UUID uuid) {
        IndividualTimerManager.stop(uuid);
        ParkourMapStateIndividual individualState = parkourMapStateIndividual.remove(uuid);
        if (individualState != null) {
            final String map = individualState.getName();
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null) hideMap(player, map);
            Kit.getAsyncScheduler().runNow(plugin, t-> taskAnimation.entrySet().removeIf(entry -> {
                MapAnimationKey key = entry.getKey();
                if (key.getMapName().equals(map) && key.getMode()==Type.INDIVIDUAL && key.getUuid().equals(uuid)) {
                    entry.getValue().cancel();
                    return true;
                }
                return false;
            }));
        }else {
            ParkourMapStateGlobal state = parkourMapStatesGlobal.values().stream()
                    .filter(s -> s.getPlayersMap().containsKey(uuid))
                    .findFirst()
                    .orElse(null);
            if (state == null) return;

            state.removePlayer(uuid);

            final String map = state.getName();
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null) hideMap(player, map);

            final ProgressTracker tracker = ProgressTrackerManager.get(map);
            if (tracker != null) {
                tracker.removePlayer(uuid);
                if (getOnlinePlayersInMap(map).isEmpty()) {
                    CheckpointBase.removeCheckpoints(map);
                }
                if (tracker.getSortedByProgress(Collections.emptyList()).isEmpty()) {
                    ProgressTrackerManager.remove(map);
                }
            }

            GlobalTimerManager.getViewingMap(uuid).ifPresent(viewingMap -> {
                GlobalTimerManager.removeViewer(uuid);
                if (GlobalTimerManager.getViewersOf(viewingMap).isEmpty()) {
                    stopGlobal(viewingMap);
                }
            });
        }
        registerOrUnregisterListener();
    }

    public static void stopGlobal(final String map) {
        GlobalTimerManager.stop(map);
        ParkourMapStateGlobal state = parkourMapStatesGlobal.remove(map);
        if (state != null) {

            for (UUID uuid_game : state.getPlayersMap().keySet()) {
                Player p = Bukkit.getPlayer(uuid_game);
                hideMap(p, map);
                if (p != null && p.isOnline()) {
                    p.sendMessage("§c¡Se ha finalizado el Parkour!");
                }
            }
            state.getPlayersMap().clear();
        }

        Gui.updateItemInLobbyInventories(map);
        Kit.getAsyncScheduler().runNow(plugin, t-> taskAnimation.entrySet().removeIf(entry -> {
            MapAnimationKey key = entry.getKey();
            if (key.getMapName().equals(map) && key.getMode()==Type.GLOBAL) {
                entry.getValue().cancel();
                return true;
            }
            return false;
        }));

    }

    public static @NotNull List<Location> getFinishPoints(final String map){
        return new Rules(map).getEndPoints();
    }

    public static @Nullable Location getRandomSpawn(final String map) {
        final Rules rules = new Rules(map);
        final List<Location> spawnPoints = rules.getSpawnsPoints();

        if (spawnPoints.isEmpty()) return null;

        final Location random = spawnPoints.get(new Random().nextInt(spawnPoints.size())).clone();
        random.add(0, 1, 0);
        return random;
    }

    public static @NotNull Optional<String> getMapIfInParkour(final @NotNull UUID uuid) {
        ParkourMapStateIndividual individualState = parkourMapStateIndividual.get(uuid);
        if (individualState != null && individualState.isInGame()) {
            return Optional.of(individualState.getName());
        }

        for (ParkourMapStateGlobal state : parkourMapStatesGlobal.values()) {
            if (state.containsPlayer(uuid)) {
                return Optional.of(state.getName());
            }
        }
        return Optional.empty();
    }

    public static @Nullable Location getSpawnPlayer(final @NotNull UUID uuid) {
        final ParkourMapStateIndividual individualState = parkourMapStateIndividual.get(uuid);
        if (individualState != null) {
            final ParkourPlayerData data = individualState.getData();
            return data != null ? data.getSpawnLocation() : null;
        }
        for (ParkourMapStateGlobal state : parkourMapStatesGlobal.values()) {
            final ParkourPlayerData data = state.getPlayerData(uuid);
            if (data != null) {
                return data.getSpawnLocation();
            }
        }
        return null;
    }

    public static boolean isAutoReconnect(final String map){
        final Rules rules = new Rules(map);
        return rules.isAutoReconnectEnabled();
    }

    public static boolean canMoveGlobal(final String map) {
        ParkourMapStateGlobal state = parkourMapStatesGlobal.get(map);
        if (state != null) {
            return state.canMove();
        }
        return true;
    }

    public static boolean canMoveIndividual(UUID uuid) {
        ParkourMapStateIndividual individualState = parkourMapStateIndividual.get(uuid);
        if (individualState != null) {
            return individualState.canMove();
        }
        return true;
    }

    public static Type getTypePlayer(final @NotNull Player player, final @NotNull String nameMap) {
        final UUID uuid = player.getUniqueId();
        ParkourMapStateIndividual individualState = parkourMapStateIndividual.get(uuid);
        if (individualState != null && individualState.isInGame()
                && individualState.getName().equals(nameMap)) {
            return Type.INDIVIDUAL;
        }
        ParkourMapStateGlobal state = parkourMapStatesGlobal.get(nameMap);
        if (state != null && state.containsPlayer(uuid)) {
            return Type.GLOBAL;
        }
        return Type.DEFAULT;
    }
}