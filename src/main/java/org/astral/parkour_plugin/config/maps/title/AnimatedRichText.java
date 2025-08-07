package org.astral.parkour_plugin.config.maps.title;

import java.util.List;

public final class AnimatedRichText {
    private final List<RichText> frames;
    private final int updateDelaySeconds;


    public AnimatedRichText(List<RichText> frames, int updateDelaySeconds) {
        this.frames = frames;

        this.updateDelaySeconds = updateDelaySeconds;
    }

    public List<RichText> getFrames() {
        return frames;
    }

    public int getUpdateDelaySeconds() {
        return updateDelaySeconds;
    }
}