package org.astral.parkour_plugin.timer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class IndividualTimerManager {

    private static final Map<UUID, TimerData> timerDataMap = new HashMap<>();

    public static void start(UUID uuid, boolean countdownMode, int timeLimitSeconds) {
        Timer timer = new Timer(countdownMode, timeLimitSeconds, 0L);
        timerDataMap.put(uuid, new TimerData(timer, 0L, false));
    }

    public static void pause(UUID uuid) {
        TimerData data = timerDataMap.get(uuid);
        if (data == null) return;
        long elapsed = data.getTimer().getElapsedMillis();
        timerDataMap.put(uuid, new TimerData(data.getTimer(), elapsed, true));
    }

    public static void resume(UUID uuid, boolean countdownMode, int timeLimitSeconds) {
        TimerData data = timerDataMap.get(uuid);
        if (data == null) return;
        long elapsed = data.getElapsedMillis();
        Timer resumedTimer = new Timer(countdownMode, timeLimitSeconds, elapsed);
        timerDataMap.put(uuid, new TimerData(resumedTimer, elapsed, false));
    }

    public static void stop(@NotNull UUID uuid) {
        timerDataMap.remove(uuid);
    }

    public static @Nullable Timer get(@NotNull UUID uuid) {
        TimerData data = timerDataMap.get(uuid);
        return data != null ? data.getTimer() : null;
    }

    public static boolean isInPause(UUID uuid) {
        TimerData data = timerDataMap.get(uuid);
        return data != null && data.isPause();
    }

    public static boolean isRunning(@NotNull UUID uuid) {
        return timerDataMap.containsKey(uuid);
    }

    private static class TimerData {
        private final Timer timer;
        private final long elapsedMillis;
        private final boolean pause;

        public TimerData(Timer timer, long elapsedMillis, boolean pause) {
            this.timer = timer;
            this.elapsedMillis = elapsedMillis;
            this.pause = pause;
        }

        public Timer getTimer() {
            return timer;
        }

        public long getElapsedMillis() {
            return elapsedMillis;
        }

        public boolean isPause() {return pause;}
    }
}