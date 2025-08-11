package org.astral.parkour_plugin.timer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class GlobalTimerManager {

    private static final Map<String, TimerData> timers = new HashMap<>();
    private static final Map<UUID, String> viewers = new HashMap<>();

    public static void start(String map, boolean countdownMode, int timeLimitSeconds) {
        Timer timer = new Timer(countdownMode, timeLimitSeconds, 0L);
        timers.put(map, new TimerData(timer, 0L, false));
    }

    public static void pause(String map) {
        TimerData data = timers.get(map);
        if (data == null) return;
        long elapsed = data.getTimer().getElapsedMillis();
        timers.put(map, new TimerData(data.getTimer(), elapsed, true));
    }

    public static void resume(String map, boolean countdownMode, int timeLimitSeconds) {
        TimerData data = timers.get(map);
        if (data == null) return;
        long elapsed = data.getElapsedMillis();
        Timer resumedTimer = new Timer(countdownMode, timeLimitSeconds, elapsed);
        timers.put(map, new TimerData(resumedTimer, elapsed, false));
    }

    public static boolean isInPause(String map) {
        TimerData data = timers.get(map);
        return data != null && data.isPause();
    }

    public static boolean isRunning(@NotNull String map) {
        return timers.containsKey(map);
    }

    public static @NotNull Set<String> getActiveMaps() {
        return timers.keySet();
    }

    public static void stop(@NotNull String map) {
        timers.remove(map);
        viewers.entrySet().removeIf(entry -> entry.getValue().equals(map));
    }

    public static @Nullable Timer get(String map) {
        TimerData data = timers.get(map);
        return data != null ? data.getTimer() : null;
    }

    public static @NotNull List<String> getListTimers(){
        return new ArrayList<>(timers.keySet());
    }

    public static void addViewer(UUID uuid, @NotNull String map) {
        viewers.put(uuid, map);
    }

    public static void removeViewer(UUID uuid) {
        viewers.remove(uuid);
    }

    public static @NotNull Optional<String> getViewingMap(UUID uuid) {
        return Optional.ofNullable(viewers.get(uuid));
    }

    public static @NotNull Set<UUID> getViewersOf(@NotNull String map) {
        Set<UUID> result = new HashSet<>();
        for (Map.Entry<UUID, String> entry : viewers.entrySet()) {
            if (entry.getValue().equals(map)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public static boolean hasAnyTimerRunning() {
        return !timers.isEmpty();
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
