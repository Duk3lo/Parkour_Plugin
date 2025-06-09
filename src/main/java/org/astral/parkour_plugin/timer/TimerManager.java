package org.astral.parkour_plugin.timer;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TimerManager {

    private static final Map<Player, Timer> timers = new HashMap<>();

    public static void start(Player player, boolean countdownMode, int timeLimitSeconds) {
        timers.put(player, new Timer(countdownMode, timeLimitSeconds));
    }

    public static String stop(Player player, String format) {
        Timer timer = timers.remove(player);
        return timer != null ? timer.getFormattedTime(format) : "00:00.000";
    }

    public static Timer get(Player player) {
        return timers.get(player);
    }

    public static boolean isRunning(Player player) {
        return timers.containsKey(player);
    }

    public static void reset(Player player) {
        timers.remove(player);
    }
}