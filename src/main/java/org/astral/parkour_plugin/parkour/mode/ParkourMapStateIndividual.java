package org.astral.parkour_plugin.parkour.mode;

import org.astral.parkour_plugin.config.maps.items.ParkourItem;
import org.astral.parkour_plugin.config.maps.title.AnimatedRichText;
import org.astral.parkour_plugin.parkour.ParkourPlayerData;

import java.util.*;

public final class ParkourMapStateIndividual {
    private final String name;
    private boolean inGame;
    private boolean canMove;
    private AnimatedRichText animatedRichText;
    private ParkourPlayerData data;
    private int timeLimit;
    private boolean isCountdown;
    private String format;
    private boolean displayActionBarTimer;
    private boolean isTimerEnable;
    private Set<ParkourItem> itemTypeParkourItemMap;

    public ParkourMapStateIndividual(String name){
        this.name = name;
        inGame = false;
        canMove = false;
        animatedRichText = new AnimatedRichText(Collections.emptyList(), 0);
        timeLimit = -1;
        isCountdown = false;
        format = "{minutes}:{seconds}:{millis}";
        displayActionBarTimer = false;
        isTimerEnable = true;
        itemTypeParkourItemMap = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public AnimatedRichText getAnimatedRichText() {
        return animatedRichText;
    }

    public void setAnimatedRichText(AnimatedRichText animatedRichText) {
        this.animatedRichText = animatedRichText;
    }

    public boolean canMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public ParkourPlayerData getData() {
        return data;
    }

    public void setData(ParkourPlayerData data) {
        this.data = data;
    }

    public boolean isDisplayActionBarTimer() {
        return displayActionBarTimer;
    }

    public void setDisplayActionBarTimer(boolean displayActionBarTimer) {
        this.displayActionBarTimer = displayActionBarTimer;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isCountdown() {
        return isCountdown;
    }

    public void setCountdown(boolean countdown) {
        isCountdown = countdown;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isTimerEnable() {
        return isTimerEnable;
    }

    public void setTimerEnable(boolean timerEnable) {
        isTimerEnable = timerEnable;
    }

    public Set<ParkourItem> getItemTypeParkourItemMap() {
        return itemTypeParkourItemMap;
    }

    public void setItemTypeParkourItemMap(Set<ParkourItem> itemTypeParkourItemMap) {
        this.itemTypeParkourItemMap = itemTypeParkourItemMap;
    }
}
