package org.astral.parkour_plugin.gui.visor.armorStand;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;

public final class PacketStructureArmorStand {
    private final int entityIdPacket;
    private final String name;
    private final Location location;
    private final PacketContainer entityPacket;
    private final PacketContainer metadataPacket;

    PacketStructureArmorStand(final int entityIdPacket, final String name, final Location location , final PacketContainer entityPacket, final PacketContainer metadata){
        this.entityIdPacket = entityIdPacket;
        this.name = name;
        this.location = location;
        this.entityPacket = entityPacket;
        this.metadataPacket = metadata;
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

    PacketContainer getMetadataPacket(){
        return metadataPacket;
    }
}
