package org.astral.parkour_plugin.actiobar;

import org.astral.parkour_plugin.compatibilizer.adapters.ActionBarApi;
import org.astral.parkour_plugin.textcomponent.ColorUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ActionBar {

    private final String message;

    public ActionBar(@NotNull String message) {
        this(message, '§');
    }

    public ActionBar(@NotNull String message, char alternateChar) {
        this.message = ColorUtil.compileColors(message.replace(alternateChar, '§'));
    }

    public void send(@NotNull Player player) {
        try {
            player.sendActionBar(message);
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            ActionBarApi.sendActionBar(player, message);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el actionbar: " + e.getMessage(), e);
        }
    }
}