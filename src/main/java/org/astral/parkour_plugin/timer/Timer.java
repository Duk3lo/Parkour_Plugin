package org.astral.parkour_plugin.timer;

import org.jetbrains.annotations.NotNull;

public class Timer {
    private final boolean countdownMode;
    private final long timeLimitMillis;
    private final long initialElapsed;
    private final long resumedAt;
    private String format = "{minutes}:{seconds}.{millis}";

    public Timer(boolean countdownMode, int timeLimitSeconds, long initialElapsed) {
        this.countdownMode = countdownMode;
        this.timeLimitMillis = timeLimitSeconds * 1000L;
        this.initialElapsed = initialElapsed;
        this.resumedAt = System.currentTimeMillis();
    }

    public long getElapsedMillis() {
        return initialElapsed + (System.currentTimeMillis() - resumedAt);
    }

    public long getRemainingMillis() {
        return Math.max(0, timeLimitMillis - getElapsedMillis());
    }

    public boolean isCountdownFinished() {
        return countdownMode && getRemainingMillis() <= 0;
    }

    public int getMinutes() {
        long time = countdownMode ? getRemainingMillis() : getElapsedMillis();
        return (int) ((time / 1000) / 60);
    }

    public int getSeconds() {
        long time = countdownMode ? getRemainingMillis() : getElapsedMillis();
        return (int) ((time / 1000) % 60);
    }

    public int getMilliseconds() {
        long time = countdownMode ? getRemainingMillis() : getElapsedMillis();
        return (int) (time % 1000);
    }

    public @NotNull String getFormattedTime() {
        return format
                .replace("{minutes}", String.format("%02d", getMinutes()))
                .replace("{seconds}", String.format("%02d", getSeconds()))
                .replace("{millis}", String.format("%03d", getMilliseconds()));
    }

    public void setFormat(final @NotNull String format) {
        this.format = format;
    }
}