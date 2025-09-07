package org.astral.parkour_plugin.parkour;


import org.bukkit.Location;

public final class ParkourPlayerData {

    private final Location spawnLocation;
    private byte getCheckpointId;

    public ParkourPlayerData(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }


    public byte getGetCheckpointId() {
        return getCheckpointId;
    }

    public void setGetCheckpointId(byte getCheckpointId) {
        this.getCheckpointId = getCheckpointId;
    }
}