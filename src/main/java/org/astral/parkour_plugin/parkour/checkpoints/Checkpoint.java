package org.astral.parkour_plugin.parkour.checkpoints;

import org.astral.parkour_plugin.config.maps.items.ParkourItem;
import org.astral.parkour_plugin.config.maps.items.ParkourItemType;
import org.astral.parkour_plugin.parkour.Type.Type;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Checkpoint {

    private final Set<UUID> players = new HashSet<>();
    private final Set<Location> locations;

    private double maxY;
    private double minY;
    private Type type;
    private final byte id;
    private Map<ParkourItemType, ParkourItem> itemMap;

    public Checkpoint(final Location location, final byte id) {
        this.locations = Collections.singleton(location);
        this.id = id;
    }

    public Checkpoint(final Set<Location> locations, final byte id) {
        this.locations = locations;
        this.id = id;
    }

    public Location getLocation() {
        return locations.stream().findFirst().orElse(null);
    }

    public @NotNull Set<Location> getLocations() {
        return new HashSet<>(locations);
    }

    public Set<UUID> getPlayers(){
        return players;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public byte getId() {
        return id;
    }


    public Map<ParkourItemType, ParkourItem> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<ParkourItemType, ParkourItem> itemMap) {
        this.itemMap = itemMap;
    }
}
