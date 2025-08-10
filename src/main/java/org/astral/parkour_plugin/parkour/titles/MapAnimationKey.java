package org.astral.parkour_plugin.parkour.titles;

import org.astral.parkour_plugin.parkour.Type.Type;

import java.util.UUID;

public final class MapAnimationKey {

    private final String mapName;
    private final Type type;
    private final UUID uuid;


    public MapAnimationKey(String mapName, Type type) {
        this(mapName, type, null);
    }

    public MapAnimationKey(String mapName, Type type, final UUID uuid) {
        this.mapName = mapName;
        this.type = type;
        this.uuid = uuid;
    }

    public String getMapName() {
        return mapName;
    }

    public Type getMode() {
        return type;
    }

    public UUID getUuid() {
        return uuid;
    }
}