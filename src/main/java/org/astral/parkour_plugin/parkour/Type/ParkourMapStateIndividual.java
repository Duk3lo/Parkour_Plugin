package org.astral.parkour_plugin.parkour.Type;

import org.astral.parkour_plugin.config.maps.title.AnimatedRichText;
import org.astral.parkour_plugin.parkour.ParkourPlayerData;

import java.util.Collections;

public final class ParkourMapStateIndividual {
    private final String name;
    private boolean inGame;
    private boolean canMove;
    private boolean displayActionBarTimer;
    private AnimatedRichText animatedRichText;
    private ParkourPlayerData data;

    public ParkourMapStateIndividual(String name){
        this.name = name;
        inGame = false;
        canMove = false;
        displayActionBarTimer = false;
        animatedRichText = new AnimatedRichText(Collections.emptyList(), 0);
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
}
