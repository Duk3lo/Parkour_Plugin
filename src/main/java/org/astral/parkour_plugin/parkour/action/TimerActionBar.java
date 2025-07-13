package org.astral.parkour_plugin.parkour.action;

import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.actiobar.ActionBar;
import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.parkour.ParkourManager;
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
    private static final Set<String> mapsAlreadyNotified = new HashSet<>();
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

    private static @NotNull String getBlinkingColor(final @NotNull String baseHex) {
        double periodMillis = 1000.0;
        double angle = 2.0 * Math.PI * (System.currentTimeMillis() % (long) periodMillis) / periodMillis;
        double oscillation = (Math.sin(angle) + 1.0) / 2.0;
        double factor = 0.4 + (1.0 - 0.4) * oscillation;
        return darkenColor(baseHex, factor);
    }

    private static @NotNull String darkenColor(final @NotNull String hex, final double factor) {
        int r = (int) (Integer.parseInt(hex.substring(0, 2), 16) * factor);
        int g = (int) (Integer.parseInt(hex.substring(2, 4), 16) * factor);
        int b = (int) (Integer.parseInt(hex.substring(4, 6), 16) * factor);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return String.format("%02X%02X%02X", r, g, b);
    }

    public static void starIndividualTimer(final @NotNull Rules rules, final @NotNull UUID uuid) {
        int timeLimit = rules.getIndividualTimeLimit();
        boolean isCountdown = rules.isIndividualCountdownEnabled();

        if (IndividualTimerManager.isRunning(uuid)) {
            IndividualTimerManager.resume(uuid, isCountdown, timeLimit);
        } else {
            IndividualTimerManager.start(uuid, isCountdown, timeLimit);
        }

        if (rules.isIndividualActionBarTimerDisplayEnabled()) {
            activeActionBars.put(uuid, rules.getIndividualTimerFormat());

            if (individualActionBarTask == null || individualActionBarTask.isCancelled()) {
                individualActionBarTask = Kit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> {

                    boolean someoneOnline = activeActionBars.keySet().stream()
                            .map(Bukkit::getPlayer)
                            .anyMatch(p -> p != null && p.isOnline());

                    if (!someoneOnline) {
                        scheduledTask.cancel();
                        individualActionBarTask = null;
                        return;
                    }

                    Iterator<Map.Entry<UUID, String>> iterator = activeActionBars.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<UUID, String> entry = iterator.next();
                        UUID uuid_game = entry.getKey();
                        String format = entry.getValue();
                        Player p = Bukkit.getPlayer(uuid_game);

                        if (p == null || !p.isOnline() || !IndividualTimerManager.isRunning(uuid_game)) {
                            iterator.remove();
                            continue;
                        }

                        Timer timer = IndividualTimerManager.get(uuid_game);
                        if (timer == null) continue;

                        boolean timeFinished = (isCountdown && timer.isCountdownFinished()) ||
                                (!isCountdown && timeLimit > 0 && timer.getElapsedMillis() >= timeLimit * 1000L);

                        String formatForDisplay = timeFinished ? format.replace("{millis}", "000") : format;
                        double progress = getProgress(timeLimit, isCountdown, timer);
                        long remainingMillis = isCountdown ? timer.getRemainingMillis() : (timeLimit * 1000L - timer.getElapsedMillis());

                        String hexColor = getDynamicColor(progress);
                        if (remainingMillis <= 10000) {
                            hexColor = getBlinkingColor(hexColor);
                        }

                        timer.setFormat(formatForDisplay);
                        String formatted = ColorUtil.compileColors("<#" + hexColor + ">" + timer.getFormattedTime());

                        if (rules.isIndividualActionBarTimerDisplayEnabled()) {
                            new ActionBar(formatted).send(p);
                        }

                        if (timeFinished) {
                            p.sendMessage("§c¡Se acabó el tiempo!");
                            IndividualTimerManager.stop(uuid_game);
                            iterator.remove();
                            ParkourManager.removePlayerParkour(uuid_game);
                        }
                    }
                }, 0L, 150L, TimeUnit.MILLISECONDS);
            }
        }
    }


    public static void startGlobalTimer(final @NotNull Rules rules, final UUID uuid) {
        int timeLimit = rules.getGlobalTimeLimit();
        boolean isCountdown = rules.isGlobalCountdownEnabled();
        String mapName = rules.getMapName();

        if (!GlobalTimerManager.isRunning(mapName)) {
            GlobalTimerManager.start(mapName, isCountdown, timeLimit);
        }

        if (rules.isGlobalActionBarTimerDisplayEnabled()) {
            GlobalTimerManager.addViewer(uuid, mapName);
            if (globalActionBarTask == null || globalActionBarTask.isCancelled()) {
                globalActionBarTask = Kit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> {
                    if (!GlobalTimerManager.hasAnyTimerRunning()) {
                        scheduledTask.cancel();
                        globalActionBarTask = null;
                        return;
                    }

                    for (String map : GlobalTimerManager.getActiveMaps()) {
                        final Timer timer = GlobalTimerManager.get(map);
                        boolean timeFinished = (isCountdown && Objects.requireNonNull(timer).isCountdownFinished()) ||
                                (!isCountdown && timeLimit > 0 && Objects.requireNonNull(timer).getElapsedMillis() >= timeLimit * 1000L);

                        double progress = getProgress(timeLimit, isCountdown, timer);
                        long remainingMillis = isCountdown ? timer.getRemainingMillis() : (timeLimit * 1000L - Objects.requireNonNull(timer).getElapsedMillis());

                        String hexColor = getDynamicColor(progress);
                        if (remainingMillis <= 10000) {
                            hexColor = getBlinkingColor(hexColor);
                        }

                        String format = rules.getGlobalTimerFormat();
                        String formatForDisplay = timeFinished ? format.replace("{millis}", "000") : format;
                        timer.setFormat(formatForDisplay);

                        String formatted = ColorUtil.compileColors("<#" + hexColor + ">" + timer.getFormattedTime());

                        if (rules.isGlobalActionBarTimerDisplayEnabled()) {
                            for (UUID uuid_game : GlobalTimerManager.getViewersOf(map)) {
                                Player p = Bukkit.getPlayer(uuid_game);
                                if (p != null && p.isOnline()) {
                                    new ActionBar(formatted).send(p);
                                }
                            }
                        }

                        if (timeFinished && !mapsAlreadyNotified.contains(map)) {
                            mapsAlreadyNotified.add(map);
                            for (UUID uuid_game : GlobalTimerManager.getViewersOf(map)) {
                                Player p = Bukkit.getPlayer(uuid_game);
                                if (p != null && p.isOnline()) {
                                    p.sendMessage("§c¡Se acabó el tiempo global!");
                                }
                                ParkourManager.removePlayerParkour(uuid_game);
                            }
                            GlobalTimerManager.stop(map);
                            mapsAlreadyNotified.remove(map);
                        }
                    }
                }, 0L, 150L, TimeUnit.MILLISECONDS);
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
