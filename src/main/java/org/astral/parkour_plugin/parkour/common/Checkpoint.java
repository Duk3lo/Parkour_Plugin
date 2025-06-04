package org.astral.parkour_plugin.parkour.common;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Deprecated
public final class Checkpoint {

    // Areas
    private final Object areaOrLocation;
    private final double minFallY, maxFallY;
    private final boolean isArea;

    // Maps
    private final HashMap<String, Set<Player>> players;

    public Checkpoint(final Object areaOrLocation, final double minFallY, final double maxFallY, final boolean isArea) {
        this.areaOrLocation = areaOrLocation;
        this.minFallY = minFallY;
        this.maxFallY = maxFallY;
        this.isArea = isArea;
        this.players = new HashMap<>();
    }

    public double getMinFallY() {
        return minFallY;
    }

    public double getMaxFallY(){
        return maxFallY;
    }

    public boolean isArea(){
        return isArea;
    }

    public HashMap<String, Set<Player>> getPlayers() {
        return players;
    }

    public @Nullable Location getLocation() {
        if (areaOrLocation instanceof Location && !isArea) {
            return (Location) areaOrLocation;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public @Nullable List<Location> getLocations() {
        if (areaOrLocation instanceof List<?> && isArea) {
            final List<?> list = (List<?>) areaOrLocation;
            if (list.stream().allMatch(item -> item instanceof Location)) {
                return (List<Location>) list;
            }
        }
        return null;
    }

    public @Nullable Location centerLocationOfXZ() {
        final List<Location> locations = getLocations();
        if (locations == null || locations.isEmpty()) return null;

        double sumX = 0;
        double sumZ = 0;

        for (final Location loc : locations) {
            sumX += loc.getX();
            sumZ += loc.getZ();
        }

        final double centerX = sumX / locations.size();
        final double centerZ = sumZ / locations.size();

        final World world = locations.get(0).getWorld();
        final double centerY = locations.get(0).getY();

        return new Location(world, centerX, centerY, centerZ);
    }
}