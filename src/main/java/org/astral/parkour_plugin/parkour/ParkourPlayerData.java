package org.astral.parkour_plugin.parkour;


import org.bukkit.Location;

public final class ParkourPlayerData {

    private final Location spawnLocation;

    public ParkourPlayerData(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }
}