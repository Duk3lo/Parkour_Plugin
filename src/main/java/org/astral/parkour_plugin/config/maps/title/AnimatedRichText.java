package org.astral.parkour_plugin.config.maps.title;

import java.util.List;

public final class AnimatedRichText {
    private final List<RichText> frames;
    private final boolean repeat;
    private final int updateDelaySeconds;

    public AnimatedRichText(List<RichText> frames, boolean repeat, int updateDelaySeconds) {
        this.frames = frames;
        this.repeat = repeat;
        this.updateDelaySeconds = updateDelaySeconds;
    }

    public List<RichText> getFrames() {
        return frames;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public int getUpdateDelaySeconds() {
        return updateDelaySeconds;
    }
}