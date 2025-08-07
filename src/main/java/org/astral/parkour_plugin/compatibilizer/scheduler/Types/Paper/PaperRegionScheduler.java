package org.astral.parkour_plugin.compatibilizer.scheduler.Types.Paper;

import org.astral.parkour_plugin.compatibilizer.scheduler.Core.RegionScheduler;
import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.astral.parkour_plugin.compatibilizer.scheduler.Types.SimpleScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PaperRegionScheduler implements RegionScheduler {

    @Override
    public void execute(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Runnable run) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.loadChunk(chunkX, chunkZ);
            }
            run.run();
        });
    }

    @Override
    public ScheduledTask run(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> task) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, false);

        int taskId = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (scheduledTask.isCancelled()) {
                return;
            }
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.loadChunk(chunkX, chunkZ);
            }
            task.accept(scheduledTask);
        }).getTaskId();

        scheduledTask.setPaperTaskId(taskId);
        scheduledTask.start();
        return scheduledTask;
    }

    @Override
    public @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> task, long delayTicks) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, false);
        int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (scheduledTask.isCancelled()) {
                return;
            }
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.loadChunk(chunkX, chunkZ);
            }
            task.accept(scheduledTask);
        }, delayTicks).getTaskId();
        scheduledTask.setPaperTaskId(taskId);
        scheduledTask.start();
        return scheduledTask;
    }

    @Override
    public @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> task, long initialDelayTicks, long periodTicks) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, true);

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (scheduledTask.isCancelled()) {
                return;
            }
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.loadChunk(chunkX, chunkZ);
            }
            task.accept(scheduledTask);
        }, initialDelayTicks, periodTicks).getTaskId();

        scheduledTask.setPaperTaskId(taskId);
        scheduledTask.start();
        return scheduledTask;
    }
}