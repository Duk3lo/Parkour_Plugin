package org.astral.parkour_plugin.parkour.Type;


import org.astral.parkour_plugin.config.maps.title.AnimatedRichText;
import org.astral.parkour_plugin.gui.Gui;
import org.astral.parkour_plugin.gui.tools.LobbyStatus;
import org.astral.parkour_plugin.parkour.ParkourPlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ParkourMapStateGlobal {
    private final Map<UUID, ParkourPlayerData> playersInParkour = new HashMap<>();
    private final String name;

    private boolean inGame;
    private boolean canMove;
    private boolean waitingPlayers;
    private boolean startingSoon;
    private final int maxPlayers;
    private final int minPlayers;
    private int limitTimeWait;
    private boolean displayWaitingPlayer;
    private String formatWaiting;
    private AnimatedRichText animatedRichText;
    private LobbyStatus lobbyStatus;
    private int timeLimit;
    private boolean isCountdown;
    private String format;
    private boolean displayActionBarTimer;

    private int timerWaiting = 0;

    public ParkourMapStateGlobal(String name, int minPlayers, int maxPlayers) {
        this.name = name;
        this.minPlayers = (minPlayers <= 0) ? 1 : minPlayers;
        this.maxPlayers = maxPlayers;
        inGame = false;
        canMove = false;
        waitingPlayers = false;
        startingSoon = false;
        limitTimeWait = 0;
        displayWaitingPlayer = true;
        formatWaiting = "§eEsperando jugadores... ({current}/{required})";
        animatedRichText = new AnimatedRichText(Collections.emptyList(), 0);
        timeLimit = -1;
        isCountdown = false;
        format = "{minutes}:{seconds}:{millis}";
        displayActionBarTimer = true;
        updateLobbyItemGlobal();
    }

    public void addPlayer(UUID uuid ,ParkourPlayerData data){
        playersInParkour.put(uuid, data);
        updateLobbyItemGlobal();
    }

    public ParkourPlayerData getPlayer(UUID uuid) {
        return playersInParkour.get(uuid);
    }

    public @NotNull Set<UUID> getAllPlayers() {
        return playersInParkour.keySet();
    }

    public Map<UUID, ParkourPlayerData> getPlayersMap() {
        return playersInParkour;
    }

    public boolean containsPlayer(UUID uuid) {
        return playersInParkour.containsKey(uuid);
    }

    public String getName() {
        return name;
    }

    public ParkourPlayerData getPlayerData(UUID uuid) {
        return playersInParkour.get(uuid);
    }

    public void removePlayer(UUID uuid){
        playersInParkour.remove(uuid);
        updateLobbyItemGlobal();
    }

    // --- Getters y setters principales ---

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        if (this.inGame != inGame) {
            this.inGame = inGame;
            if (inGame){
                lobbyStatus = LobbyStatus.IN_GAME;
            }
            updateLobbyItemGlobal();
        }
    }

    public boolean canMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public boolean isWaitingPlayers() {
        return waitingPlayers;
    }

    public void setWaitingPlayers(boolean waitingPlayers) {
        if (this.waitingPlayers != waitingPlayers) {
            this.waitingPlayers = waitingPlayers;
            updateLobbyItemGlobal();
        }
    }

    public void setStartingSoon(boolean startingSoon) {
        if (this.startingSoon != startingSoon) {
            if (startingSoon){
                lobbyStatus = LobbyStatus.STARTING;
            }
            this.startingSoon = startingSoon;
            updateLobbyItemGlobal();
        }
    }

    public int getCurrentPlayers() {
        return playersInParkour.size();
    }

    // --- Lógica de límite de jugadores ---

    public boolean hasUnlimitedPlayers() {
        return maxPlayers == -1;
    }

    public int getEffectiveMaxPlayers() {
        return hasUnlimitedPlayers() ? Integer.MAX_VALUE : Math.max(maxPlayers, minPlayers);
    }

    public int getMinPlayers(){
        return minPlayers;
    }

    public boolean isFull() {
        if (hasUnlimitedPlayers()) return false;
        return getCurrentPlayers() >= maxPlayers;
    }

    public LobbyStatus getStatus() {
        if (lobbyStatus == null){
            return LobbyStatus.WAITING;
        }
        return lobbyStatus;
    }

    private void updateLobbyItemGlobal() {
        Gui.updateItemInLobbyInventories(name);
    }

    public boolean isDisplayActionBarTimer() {
        return displayActionBarTimer;
    }

    public void setDisplayActionBarTimer(boolean displayActionBarTimer) {
        this.displayActionBarTimer = displayActionBarTimer;
    }

    public int getLimitTimeWait() {
        return limitTimeWait;
    }

    public void setLimitTimeWait(int limitTimeWait) {
        this.limitTimeWait = limitTimeWait;
    }

    public boolean isDisplayWaitingPlayer() {
        return displayWaitingPlayer;
    }

    public void setDisplayWaitingPlayer(boolean displayWaitingPlayer) {
        this.displayWaitingPlayer = displayWaitingPlayer;
    }

    public String getFormatWaiting() {
        return formatWaiting;
    }

    public void setFormatWaiting(String formatWaiting) {
        this.formatWaiting = formatWaiting;
    }

    //otros metodos
    private int dotFrame = 0;
    public String getAnimatedDots() {
        String[] frames = {".", "..", "..."};
        String dots = frames[0];
        dotFrame = (dotFrame + 1) % frames.length;
        return dots;
    }

    public int incrementTimer() {
        return timerWaiting++;
    }

    //public int getTimerWaiting(){return timerWaiting;}

    public AnimatedRichText getAnimatedRichText() {
        return animatedRichText;
    }

    public void setAnimatedRichText(AnimatedRichText animatedRichText) {
        this.animatedRichText = animatedRichText;
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
}