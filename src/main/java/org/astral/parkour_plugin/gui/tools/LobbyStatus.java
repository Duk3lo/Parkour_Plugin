package org.astral.parkour_plugin.gui.tools;

import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.astral.parkour_plugin.compatibilizer.adapters.MaterialApi;
import org.astral.parkour_plugin.parkour.ParkourManager;
import org.astral.parkour_plugin.parkour.mode.ParkourMapStateGlobal;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public enum LobbyStatus {

    DISABLED("Sin jugadores", ChatColor.GRAY, MaterialApi.getMaterial("WOOL", "GRAY_WOOL")),
    WAITING("Esperando jugadores", ChatColor.GREEN, MaterialApi.getMaterial("WOOL", "GREEN_WOOL")),
    IN_GAME("En juego", ChatColor.RED, MaterialApi.getMaterial("WOOL", "RED_WOOL")),
    FULL("Lleno", ChatColor.DARK_RED, MaterialApi.getMaterial("WOOL", "BLACK_WOOL")), // o PURPLE_WOOL si prefieres
    STARTING("A punto de empezar", ChatColor.YELLOW, MaterialApi.getMaterial("WOOL", "YELLOW_WOOL"));

    private final String description;
    private final ChatColor color;
    private final Material wool;

    LobbyStatus(String description, ChatColor color, Material wool) {
        this.description = description;
        this.color = color;
        this.wool = wool;
    }

    public @NotNull ItemStack toItemStack(String lobbyName) {
        final ItemStack result;

        if (ApiCompatibility.ARRAY_VERSION()[1] <= 12) {
            byte data;
            switch (this) {
                case WAITING: data = 5; break;
                case IN_GAME: data = 14; break;
                case FULL: data = 15; break;
                case STARTING: data = 4; break;
                case DISABLED:
                default: data = 8; break;
            }
            result = new ItemStack(MaterialApi.getMaterial("WOOL"), 1, data);
        } else {
            result = new ItemStack(wool);
        }

        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + lobbyName);

            // Agregamos lore dinámica
            List<String> lore = new ArrayList<>();
            lore.add(color + description);

            // ⚠️ Cargar info de jugadores usando ParkourManager
            ParkourMapStateGlobal state = ParkourManager.getMapStateGlobal(lobbyName);
            if (state != null) {
                lore.add(ChatColor.GRAY + "Jugadores: " + ChatColor.WHITE + state.getCurrentPlayers()
                        + ChatColor.GRAY + " / "
                        + ChatColor.WHITE + (state.hasUnlimitedPlayers() ? "∞" : state.getEffectiveMaxPlayers()));
            } else {
                lore.add(ChatColor.RED + "Estado no disponible");
            }

            meta.setLore(lore);
            result.setItemMeta(meta);
        }

        return result;
    }
}
