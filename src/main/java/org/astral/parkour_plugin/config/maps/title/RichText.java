package org.astral.parkour_plugin.config.maps.title;

import org.astral.parkour_plugin.textcomponent.ColorUtil;
import org.jetbrains.annotations.Nullable;

public final class RichText {
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public RichText(String title, @Nullable String subtitle) {
        this(title, subtitle, 20, 200, 20);
    }

    public RichText(String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = ColorUtil.compileColors(title);
        this.subtitle = ColorUtil.compileColors(subtitle);
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public int getStay() {
        return stay;
    }
}