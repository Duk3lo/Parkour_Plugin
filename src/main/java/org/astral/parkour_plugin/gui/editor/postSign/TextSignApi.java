package org.astral.parkour_plugin.gui.editor.postSign;

import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public interface TextSignApi {

    String Title = "Nombre del Mapa";
    List<String> Lore = Arrays.asList("", "Ingresa el", "nombre", "de Tu Mapa");

    void AddNewMap(final Player player);

    static @NotNull TextSignApi _text(final JavaPlugin plugin){
        if (ApiCompatibility.HAS_PROTOCOL()) return new ProtocolTextSign(plugin);
        else return new TextSign();
    }
}
