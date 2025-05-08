package org.astral.parkour_plugin.gui.compatible;

import org.astral.parkour_plugin.gui.Gui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.jetbrains.annotations.NotNull;

public final class ModernGuiListener implements Listener {
    @EventHandler
    public void onSwapHandItems(final @NotNull PlayerSwapHandItemsEvent event) {
        final Player player = event.getPlayer();
        if (Gui.isInEditMode(player))event.setCancelled(true);
    }
}