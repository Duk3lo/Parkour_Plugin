package org.astral.parkour_plugin.gui.visor.armorStand;

import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.astral.parkour_plugin.gui.visor.Type;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public interface ArmorStandApi {
    Map<String, Set<Player>> playersViewingMap = new HashMap<>();
    void showHolograms(final Player player, final String map,final Type type);
    void hideHolograms(final Player player, final String map, final Type type);
    void addHologram(final String map, final String name , final Location location, final Type type);
    void removeHologram(final String map, final String name, final Type type);
    void reorderArmorStandNames(final String map, final Type type);

    static @NotNull ArmorStandApi _view(final JavaPlugin plugin){
        if (ApiCompatibility.HAS_PROTOCOL())return new ProtocolArmorStand(plugin);
        else return new ArmorStandView(plugin);
    }
}