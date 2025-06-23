package org.astral.parkour_plugin.parkour;


import org.bukkit.Location;

public final class ParkourPlayerData {
    private final String mapName;
    private final Location spawnLocation;

    public ParkourPlayerData(String mapName, Location spawnLocation) {
        this.mapName = mapName;
        this.spawnLocation = spawnLocation;
    }

    public String getMapName() {
        return mapName;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }
}