package org.astral.parkour_plugin.parkour;


import org.astral.parkour_plugin.parkour.checkpoints.Checkpoint;
import org.bukkit.Location;

public final class ParkourPlayerData {

    private final Location spawnLocation;
    private Checkpoint checkpoint;

    public ParkourPlayerData(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }


    public Checkpoint getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(Checkpoint checkpoint) {
        this.checkpoint = checkpoint;
    }
}