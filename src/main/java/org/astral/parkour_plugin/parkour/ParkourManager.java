package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.compatibilizer.adapters.TeleportingApi;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.titles.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.*;

public final class ParkourManager {

    public static final Map<String, Set<Player>> playersMaps = new HashMap<>();

    public static void gotoParkourPlayer(final Player player, final String map) {
        final Optional<Location> spawn = getRandomSpawn(map);
        if (!spawn.isPresent()) {
            player.sendMessage("§cNo existe ningún lugar de aparición definido para el mapa §b" + map + "§c.");
            return;
        }

        TeleportingApi.teleport(player, spawn.get());
        final Title title = new Title("§a¡Bienvenido!", "§fMapa: §b" + map, 10, 40, 10);
        title.send(player);
        player.sendMessage("§7Has sido teletransportado al mapa §b" + map + "§7. ¡Buena suerte!");
        playersMaps.computeIfAbsent(map, k -> new HashSet<>()).add(player);
    }


    public static void exitParkour(final Player player) {
        playersMaps.entrySet().removeIf(entry -> {
            Set<Player> players = entry.getValue();
            players.remove(player);
            return players.isEmpty();
        });
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
        for (Map.Entry<String, Set<Player>> entry : playersMaps.entrySet()) {
            if (entry.getValue().contains(player)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

}