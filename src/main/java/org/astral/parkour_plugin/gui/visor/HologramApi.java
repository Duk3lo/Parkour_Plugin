package org.astral.parkour_plugin.gui.visor;

import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public interface HologramApi {
    Map<String, Set<Player>> playersViewingMap = new HashMap<>();
    void showHolograms(final Player player, final String map);
    void hideHolograms(final Player player, final String map);
    void addHologram(final String map, final String name , final Location location);
    void removeHologram(final String map, final String name);
    void reorderArmorStandNames(final String map);

    static @NotNull HologramApi _view(final JavaPlugin plugin){
        if (ApiCompatibility.HAS_PROTOCOL())return new ProtocolHologram(plugin);
        else return new Hologram(plugin);
    }
}