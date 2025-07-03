package org.astral.parkour_plugin.parkour;

public final class ParkourMapState {
    private boolean inGame;
    private boolean canMove;

    public ParkourMapState() {
        this.inGame = false;
        this.canMove = false;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public boolean canMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }
}