package org.astral.parkour_plugin.timer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class IndividualTimerManager {

    private static final Map<UUID, TimerData> timerDataMap = new HashMap<>();

    public static void start(final @NotNull Player player, final boolean countdownMode, final int timeLimitSeconds) {
        long now = System.currentTimeMillis();
        Timer timer = new Timer(countdownMode, timeLimitSeconds);
        timerDataMap.put(player.getUniqueId(), new TimerData(timer, now));
    }

    public static void resume(final @NotNull Player player, final boolean countdownMode, final int timeLimitSeconds) {
        TimerData oldData = timerDataMap.get(player.getUniqueId());
        if (oldData == null) return;

        long persistedTime = oldData.getStartTime();
        Timer resumedTimer = new Timer(countdownMode, timeLimitSeconds) {
            @Override
            public long getElapsedMillis() {
                return System.currentTimeMillis() - persistedTime;
            }
        };
        timerDataMap.put(player.getUniqueId(), new TimerData(resumedTimer, persistedTime));
    }

    public static void stop(final @NotNull Player player) {
        timerDataMap.remove(player.getUniqueId());
    }

    public static @Nullable Timer get(final @NotNull Player player) {
        TimerData data = timerDataMap.get(player.getUniqueId());
        return data != null ? data.getTimer() : null;
    }

    public static boolean isRunning(final @NotNull Player player) {
        return timerDataMap.containsKey(player.getUniqueId());
    }


    private static class TimerData {
        private final Timer timer;
        private final long startTime;

        public TimerData(Timer timer, long startTime) {
            this.timer = timer;
            this.startTime = startTime;
        }

        public Timer getTimer() {
            return timer;
        }

        public long getStartTime() {
            return startTime;
        }
    }
}
