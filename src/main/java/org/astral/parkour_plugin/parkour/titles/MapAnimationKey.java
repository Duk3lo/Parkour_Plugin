package org.astral.parkour_plugin.parkour.titles;

import org.astral.parkour_plugin.parkour.Type.Type;

public final class MapAnimationKey {

    private final String mapName;
    private final Type type;


    public MapAnimationKey(String mapName, Type type) {
        this.mapName = mapName;
        this.type = type;
    }

    public String getMapName() {
        return mapName;
    }

    public Type getMode() {
        return type;
    }

}