package org.astral.parkour_plugin.parkour;


import org.bukkit.Location;

public final class ParkourPlayerData {
    private final String mapName;
    private final Location spawnLocation;
    private final Mode mode;

    public ParkourPlayerData(String mapName, Location spawnLocation, Mode mode) {
        this.mapName = mapName;
        this.spawnLocation = spawnLocation;
        this.mode = mode;
    }

    public String getMapName() {
        return mapName;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public Mode getMode(){
        return mode;
    }
}