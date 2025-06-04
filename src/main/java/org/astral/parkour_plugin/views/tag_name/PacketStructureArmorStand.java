package org.astral.parkour_plugin.views.tag_name;

import com.comphenix.protocol.events.PacketContainer;
import org.astral.parkour_plugin.views.Type;
import org.bukkit.Location;

public final class PacketStructureArmorStand {
    private final int entityIdPacket;
    private final String name;
    private final Location location;
    private final PacketContainer entityPacket;
    private final PacketContainer metadataPacket;
    private final Type type;

    PacketStructureArmorStand(final int entityIdPacket, final String name, final Location location , final PacketContainer entityPacket, final PacketContainer metadata, final Type type){
        this.entityIdPacket = entityIdPacket;
        this.name = name;
        this.location = location;
        this.entityPacket = entityPacket;
        this.metadataPacket = metadata;
        this.type = type;
    }

    int getEntityIdPacket(){
        return entityIdPacket;
    }

    String getName(){
        return name;
    }

    Location getLocation(){
        return location;
    }

    PacketContainer getEntityPacket(){
        return entityPacket;
    }

    public Type getType() {
        return type;
    }

    PacketContainer getMetadataPacket(){
        return metadataPacket;
    }
}
