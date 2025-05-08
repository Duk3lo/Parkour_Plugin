package org.astral.parkour_plugin.compatibilizer.scheduler.Types.Paper;

import org.astral.parkour_plugin.compatibilizer.scheduler.Core.AsyncScheduler;
import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.astral.parkour_plugin.compatibilizer.scheduler.Types.SimpleScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PaperAsyncScheduler implements AsyncScheduler {

    @Override
    public @NotNull ScheduledTask runNow(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, false);

        int taskId = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!scheduledTask.isCancelled()) {
                task.accept(scheduledTask);
                scheduledTask.finish();
            }
        }).getTaskId();

        scheduledTask.setPaperTaskId(taskId);
        scheduledTask.start();
        return scheduledTask;
    }

    @Override
    public @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long delay, @NotNull TimeUnit unit) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, false);
        long delayTicks = unit.toSeconds(delay) * 20;

        int taskId = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (!scheduledTask.isCancelled()) {
                task.accept(scheduledTask);
                scheduledTask.finish();
            }
        }, delayTicks).getTaskId();

        scheduledTask.setPaperTaskId(taskId);
        scheduledTask.start();
        return scheduledTask;
    }

    @Override
    public @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long initialDelay, long period, @NotNull TimeUnit unit) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, true);
        long initialDelayTicks = unit.toSeconds(initialDelay) * 20;
        long periodTicks = unit.toSeconds(period) * 20;

        int taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!scheduledTask.isCancelled()) {
                task.accept(scheduledTask);
            }
        }, initialDelayTicks, periodTicks).getTaskId();

        scheduledTask.setPaperTaskId(taskId);
        scheduledTask.start();
        return scheduledTask;
    }

    @Override
    public void cancelTasks(@NotNull Plugin plugin) {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}