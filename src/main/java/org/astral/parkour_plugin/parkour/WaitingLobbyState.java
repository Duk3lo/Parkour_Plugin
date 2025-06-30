package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.config.maps.rules.Rules;

import java.util.concurrent.atomic.AtomicInteger;

public class WaitingLobbyState {
    private int dotFrame = 0;
    private final Rules rules;
    private final AtomicInteger timer = new AtomicInteger(0);

    public WaitingLobbyState(Rules rules) {
        this.rules = rules;
    }

    public Rules getRules() {
        return rules;
    }

    public int incrementTimer() {
        return timer.getAndIncrement();
    }

    public String getAnimatedDots() {
        String[] frames = {".", "..", "..."};
        String dots = frames[dotFrame];
        dotFrame = (dotFrame + 1) % frames.length;
        return dots;
    }
}
