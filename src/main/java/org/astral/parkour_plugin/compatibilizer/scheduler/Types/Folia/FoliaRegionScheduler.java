package org.astral.parkour_plugin.compatibilizer.scheduler.Types.Folia;

import org.astral.parkour_plugin.compatibilizer.scheduler.Core.RegionScheduler;
import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.astral.parkour_plugin.compatibilizer.scheduler.Types.SimpleScheduledTask;
import org.astral.parkour_plugin.compatibilizer.scheduler.Types.TaskAdapter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class FoliaRegionScheduler implements RegionScheduler {

    private static @Nullable Object getRegionSchedulerInstance(Plugin plugin) {
        try {
            @SuppressWarnings("JavaReflectionMemberAccess") Method method = Bukkit.class.getMethod("getRegionScheduler");
            return method.invoke(null);
        } catch (Exception e) {
            plugin.getLogger().warning("[FoliaRegionScheduler] No se encontró 'getRegionScheduler'. Folia no está disponible.");
            return null;
        }
    }

    private static @Nullable Object invokeFoliaMethod(Plugin plugin, String methodName, Object... args) {
        Object regionScheduler = getRegionSchedulerInstance(plugin);
        if (regionScheduler == null) return null;

        try {
            for (Method method : regionScheduler.getClass().getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.invoke(regionScheduler, args);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[FoliaRegionScheduler] Error al invocar " + methodName + ": " + e.getMessage());
        }

        return null;
    }

    @Override
    public void execute(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Runnable run) {
        invokeFoliaMethod(plugin, "execute", plugin, world, chunkX, chunkZ, run);
    }

    @Override
    public ScheduledTask run(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> task) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, false);

        Object foliaTask = invokeFoliaMethod(plugin, "run", plugin, world, chunkX, chunkZ, TaskAdapter.adapt(scheduledTask, task));

        if (foliaTask != null) {
            scheduledTask.setFoliaTask(foliaTask);
            scheduledTask.start();
        }

        return scheduledTask;
    }

    @Override
    public @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> task, long delayTicks) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, false);

        Object foliaTask = invokeFoliaMethod(plugin, "runDelayed", plugin, world, chunkX, chunkZ, TaskAdapter.adapt(scheduledTask, task), delayTicks);

        if (foliaTask != null) {
            scheduledTask.setFoliaTask(foliaTask);
            scheduledTask.start();
        }

        return scheduledTask;
    }

    @Override
    public @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> task, long initialDelayTicks, long periodTicks) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, true);

        Object foliaTask = invokeFoliaMethod(plugin, "runAtFixedRate", plugin, world, chunkX, chunkZ, TaskAdapter.adapt(scheduledTask, task), initialDelayTicks, periodTicks);

        if (foliaTask != null) {
            scheduledTask.setFoliaTask(foliaTask);
            scheduledTask.start();
        }

        return scheduledTask;
    }
}