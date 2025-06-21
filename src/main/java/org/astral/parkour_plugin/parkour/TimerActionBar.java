package org.astral.parkour_plugin.parkour;

import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.actiobar.ActionBar;
import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.textcomponent.ColorUtil;
import org.astral.parkour_plugin.timer.GlobalTimerManager;
import org.astral.parkour_plugin.timer.Timer;
import org.astral.parkour_plugin.timer.IndividualTimerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class TimerActionBar {

    private static final Main plugin = Main.getInstance();

    private static final Map<UUID, String> activeActionBars = new HashMap<>();
    private static ScheduledTask individualActionBarTask = null;
    private static ScheduledTask globalActionBarTask = null;

    private static final String GREEN_HEX = "00FF00";
    private static final String YELLOW_HEX = "FFFF00";
    private static final String RED_HEX = "FF0000";

    private static @NotNull String interpolateColor(final @NotNull String hex1, final @NotNull String hex2, final double ratio) {
        int r1 = Integer.parseInt(hex1.substring(0, 2), 16);
        int g1 = Integer.parseInt(hex1.substring(2, 4), 16);
        int b1 = Integer.parseInt(hex1.substring(4, 6), 16);

        int r2 = Integer.parseInt(hex2.substring(0, 2), 16);
        int g2 = Integer.parseInt(hex2.substring(2, 4), 16);
        int b2 = Integer.parseInt(hex2.substring(4, 6), 16);

        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return String.format("%02X%02X%02X", r, g, b);
    }

    private static @NotNull String getDynamicColor(final double progress) {
        if (progress >= 0.5) {
            double ratio = (1.0 - progress) * 2;
            return interpolateColor(GREEN_HEX, YELLOW_HEX, ratio);
        } else {
            double ratio = (0.5 - progress) * 2;
            return interpolateColor(YELLOW_HEX, RED_HEX, ratio);
        }
    }

    public static void starIndividualTimer(final @NotNull Rules rules, final Player player) {

        int timeLimit = rules.getTimeLimit();
        boolean isCountdown = rules.isCountdownEnabled();
        IndividualTimerManager.start(player, isCountdown, timeLimit);

        if (rules.isActionBarTimerDisplayEnabled()) {
            activeActionBars.put(player.getUniqueId(), rules.getTimerFormat());

            if (individualActionBarTask == null || individualActionBarTask.isCancelled()) {
                individualActionBarTask = Kit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> {
                    if (activeActionBars.isEmpty()) {
                        scheduledTask.cancel();
                        individualActionBarTask = null;
                        return;
                    }

                    Iterator<Map.Entry<UUID, String>> iterator = activeActionBars.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<UUID, String> entry = iterator.next();
                        UUID uuid = entry.getKey();
                        String format = entry.getValue();
                        Player p = Bukkit.getPlayer(uuid);
                        if (p == null || !p.isOnline() || !IndividualTimerManager.isRunning(p)) {
                            iterator.remove();
                            continue;
                        }

                        Timer timer = IndividualTimerManager.get(p);
                        boolean timeFinished = (isCountdown && timer.isCountdownFinished()) ||
                                (!isCountdown && timeLimit > 0 && timer.getElapsedMillis() >= timeLimit * 1000L);

                        String formatForDisplay = timeFinished ? format.replace("{millis}", "000") : format;
                        double progress = getProgress(timeLimit, isCountdown, timer);
                        String hexColor = getDynamicColor(progress);
                        timer.setFormat(formatForDisplay);
                        String formatted = ColorUtil.compileColors("<#" + hexColor + ">" + timer.getFormattedTime());

                        new ActionBar(formatted).send(p);

                        if (timeFinished) {
                            p.sendMessage("§c¡Se acabó el tiempo!");
                            IndividualTimerManager.stop(p);
                            iterator.remove();
                        }
                    }
                }, 0L, 2L, TimeUnit.MILLISECONDS);
            }
        }
    }

    public static void startGlobalTimer(final @NotNull Rules rules, final Player player) {
        int timeLimit = rules.getTimeLimit();
        boolean isCountdown = rules.isCountdownEnabled();
        String mapName = rules.getMapName();

        if (!GlobalTimerManager.isRunning(mapName)) {
            GlobalTimerManager.start(mapName, isCountdown, timeLimit);
        }

        GlobalTimerManager.addViewer(player, mapName);

        if (rules.isActionBarTimerDisplayEnabled()) {
            if (globalActionBarTask == null || globalActionBarTask.isCancelled()) {
                globalActionBarTask = Kit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> {
                    if (!GlobalTimerManager.hasAnyTimerRunning()) {
                        scheduledTask.cancel();
                        globalActionBarTask = null;
                        return;
                    }

                    for (String map : GlobalTimerManager.getActiveMaps()) {
                        final Timer timer = GlobalTimerManager.get(map);
                        boolean timeFinished = (isCountdown && timer.isCountdownFinished()) ||
                                (!isCountdown && timeLimit > 0 && timer.getElapsedMillis() >= timeLimit * 1000L);

                        double progress = getProgress(timeLimit, isCountdown, timer);
                        String hexColor = getDynamicColor(progress);
                        String formatted = ColorUtil.compileColors("<#" + hexColor + ">" + timer.getFormattedTime());

                        for (Player p : GlobalTimerManager.getViewersOf(map)) {
                            if (p.isOnline()) {
                                new ActionBar(formatted).send(p);
                            }
                        }

                        if (timeFinished) {
                            for (Player p : GlobalTimerManager.getViewersOf(map)) {
                                if (p.isOnline()) {
                                    p.sendMessage("§c¡Se acabó el tiempo global!");
                                }
                            }
                            GlobalTimerManager.stop(map);
                        }
                    }
                }, 0L, 2L, TimeUnit.MILLISECONDS);
            }
        }
    }

    private static double getProgress(int timeLimit, boolean isCountdown, Timer timer) {
        double progress;
        if (timeLimit > 0) {
            if (isCountdown) {
                progress = Math.max(0.0, Math.min(1.0, (double) timer.getRemainingMillis() / (timeLimit * 1000L)));
            } else {
                progress = Math.max(0.0, Math.min(1.0, 1.0 - ((double) timer.getElapsedMillis() / (timeLimit * 1000L))));
            }
        } else {
            progress = 1.0;
        }
        return progress;
    }
}
