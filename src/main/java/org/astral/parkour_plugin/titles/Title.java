package org.astral.parkour_plugin.titles;

import org.astral.parkour_plugin.compatibilizer.adapters.TitleApi;
import org.astral.parkour_plugin.textcomponent.ColorUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Title {

    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public Title(String title) {
        this(title, null, 20, 200, 20);
    }

    public Title(String title, @Nullable String subtitle) {
        this(title, subtitle, 20, 200, 20);
    }

    public Title(String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = ColorUtil.compileColors(title);
        this.subtitle = ColorUtil.compileColors(subtitle);
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public void send(final @NotNull Player player) {
        try {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            TitleApi.send(player, title, subtitle, fadeIn, stay, fadeOut);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
