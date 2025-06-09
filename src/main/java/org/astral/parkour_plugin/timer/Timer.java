package org.astral.parkour_plugin.timer;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;


public class Timer {

    private final long startTimeNano;
    private final boolean countdownMode;
    private final long timeLimitMillis;

    public Timer(boolean countdownMode, int timeLimitSeconds) {
        this.countdownMode = countdownMode && timeLimitSeconds > 0;
        this.timeLimitMillis = this.countdownMode ? timeLimitSeconds * 1000L : -1;
        this.startTimeNano = System.nanoTime();
    }

    public long getElapsedMillis() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNano);
    }

    public long getRemainingMillis() {
        return Math.max(0, timeLimitMillis - getElapsedMillis());
    }

    public long getMinutes() {
        long millis = countdownMode ? getRemainingMillis() : getElapsedMillis();
        return TimeUnit.MILLISECONDS.toMinutes(millis);
    }

    public long getSeconds() {
        long millis = countdownMode ? getRemainingMillis() : getElapsedMillis();
        return TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
    }

    public long getMilliseconds() {
        long millis = countdownMode ? getRemainingMillis() : getElapsedMillis();
        return millis % 1000;
    }

    public String getFormattedTime(final @NotNull String formatInput) {
        String format = formatInput;
        final String defaultFormat = "{minutes}:{seconds}.{millis}";
        if (format.isEmpty() || !(format.contains("{minutes}") || format.contains("{seconds}") || format.contains("{millis}"))) {
            format = defaultFormat;
        }
        return format
                .replace("{minutes}", String.format("%02d", getMinutes()))
                .replace("{seconds}", String.format("%02d", getSeconds()))
                .replace("{millis}", String.format("%03d", getMilliseconds()));
    }

    public boolean isCountdownFinished() {
        return countdownMode && getElapsedMillis() >= timeLimitMillis;
    }
}
