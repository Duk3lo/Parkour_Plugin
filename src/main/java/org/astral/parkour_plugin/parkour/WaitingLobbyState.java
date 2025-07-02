package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.config.maps.title.AnimatedRichText;

public class WaitingLobbyState {
    private int dotFrame = 0;
    private final Rules rules;
    private int timer = 0;
    private int preStart = 0;
    private ScheduledTask countDownTask;

    public WaitingLobbyState(Rules rules) {
        this.rules = rules;
    }

    public Rules getRules() {
        return rules;
    }

    public int incrementTimer() {
        return timer++;
    }

    public void incrementStart(){
        preStart++;
    }

    public int getStart(){
        return preStart;
    }

    public int sizeOfFramesTitle() {
        return rules.getAnimatedTitle("star_countdown")
                .map(animated -> animated.getFrames().size())
                .orElse(0);
    }

    public int getAnimatedTimerPreStar(){
        return rules.getAnimatedTitle("star_countdown").map(AnimatedRichText::getUpdateDelaySeconds).orElse(0);
    }

    public void setCountDownTask(final ScheduledTask countDownTask){
        this.countDownTask = countDownTask;
    }

    public ScheduledTask getCountDownTask() {
        return countDownTask;
    }

    public String getAnimatedDots() {
        String[] frames = {".", "..", "..."};
        String dots = frames[dotFrame];
        dotFrame = (dotFrame + 1) % frames.length;
        return dots;
    }
}
