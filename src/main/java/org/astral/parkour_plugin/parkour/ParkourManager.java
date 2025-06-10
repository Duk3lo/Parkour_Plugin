package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.actiobar.ActionBar;
import org.astral.parkour_plugin.compatibilizer.adapters.TeleportingApi;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.parkour.checkpoints.CheckpointBase;
import org.astral.parkour_plugin.titles.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ParkourManager {

    private static final Main plugin = Main.getInstance();


    public static final Map<Player, String> playersMaps = new HashMap<>();
    private static final Map<Player, Location> spawnPlayer = new HashMap<>();

    private static final Listener parkourListener = new ParkourListener();

    private static boolean active = false;

    public static void registerOrUnregisterListener() {
        boolean hasPlayers = !playersMaps.isEmpty();
        if (hasPlayers && !active) {
            plugin.getServer().getPluginManager().registerEvents(parkourListener, plugin);
            active = true;
        } else if (!hasPlayers && active) {
            HandlerList.unregisterAll(parkourListener);
            active = false;
        }
    }

    public static void starParkourHere(final @NotNull Player player, final String map) {
        final Location blockLocation = player.getLocation().clone().subtract(0, 1, 0).getBlock().getLocation();
        CheckpointBase.loadMap(map);
        addAndSave(player, blockLocation, map);
        final Rules rules = new Rules(map);
        Optional<Title> optionalTitle = rules.getStartTitle();
        optionalTitle.ifPresent(title -> title.send(player));
        rules.getMessage("start", player.getName()).ifPresent(player::sendMessage);
        player.sendActionBar("uffas");
        ActionBar actionBar = new ActionBar("uffas");
        actionBar.send(player);
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
    }

    public static void loadTimer(final Rules rules){

    }

    public static void addAndSave(final Player player, final Location location, final String map){
        playersMaps.put(player, map);
        spawnPlayer.put(player, location);
        registerOrUnregisterListener();
    }

    public static void exitParkour(final Player player) {
        playersMaps.remove(player);
        spawnPlayer.remove(player);
        registerOrUnregisterListener();
    }

    public static Optional<Location> getRandomSpawn(final String map) {
        final Rules rules = new Rules(map);
        final List<Location> spawnPoints = rules.getSpawnsPoints();

        if (spawnPoints.isEmpty()) return Optional.empty();

        final Location random = spawnPoints.get(new Random().nextInt(spawnPoints.size())).clone();
        random.add(0, 1, 0);
        return Optional.of(random);
    }

    public static @NotNull Optional<String> getMapIfInParkour(final Player player) {
        return Optional.ofNullable(playersMaps.get(player));
    }

    public static Location getSpawnPlayer(final Player player){
        return spawnPlayer.get(player);
    }

    public static boolean isAutoReconnect(final String map){
        final Rules rules = new Rules(map);
        return rules.isAutoReconnectEnabled();
    }
}