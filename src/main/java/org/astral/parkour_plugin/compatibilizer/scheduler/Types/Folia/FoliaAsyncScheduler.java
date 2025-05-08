package org.astral.parkour_plugin.compatibilizer.scheduler.Types.Folia;

import org.astral.parkour_plugin.compatibilizer.scheduler.Core.AsyncScheduler;
import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.astral.parkour_plugin.compatibilizer.scheduler.Types.SimpleScheduledTask;
import org.astral.parkour_plugin.compatibilizer.scheduler.Types.TaskAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FoliaAsyncScheduler implements AsyncScheduler {

    private static @Nullable Object getAsyncSchedulerInstance(Plugin plugin) {
        try {
            @SuppressWarnings("JavaReflectionMemberAccess") Method method = Bukkit.class.getMethod("getAsyncScheduler");
            return method.invoke(null);
        } catch (Exception e) {
            plugin.getLogger().warning("[FoliaAsyncScheduler] No se encontró 'getAsyncScheduler'. Folia no está disponible.");
            return null;
        }
    }

    private static @Nullable Object invokeFoliaMethod(Plugin plugin, String methodName, Object... args) {
        Object asyncScheduler = getAsyncSchedulerInstance(plugin);
        if (asyncScheduler == null) return null;

        try {
            for (Method method : asyncScheduler.getClass().getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.invoke(asyncScheduler, args);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[FoliaAsyncScheduler] Error al invocar " + methodName + ": " + e.getMessage());
        }

        return null;
    }

    @Override
    public @NotNull ScheduledTask runNow(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, false);

        Object foliaTask = invokeFoliaMethod(plugin, "runNow", plugin, TaskAdapter.adapt(scheduledTask, task));

        if (foliaTask != null) {
            scheduledTask.setFoliaTask(foliaTask);
            scheduledTask.start();
        }

        return scheduledTask;
    }

    @Override
    public @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long delay, @NotNull TimeUnit unit) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, false);

        Object foliaTask = invokeFoliaMethod(plugin, "runDelayed", plugin, TaskAdapter.adapt(scheduledTask, task), delay, unit);

        if (foliaTask != null) {
            scheduledTask.setFoliaTask(foliaTask);
            scheduledTask.start();
        }

        return scheduledTask;
    }

    @Override
    public @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long initialDelay, long period, @NotNull TimeUnit unit) {
        SimpleScheduledTask scheduledTask = new SimpleScheduledTask(plugin, true);

        Object foliaTask = invokeFoliaMethod(plugin, "runAtFixedRate", plugin, TaskAdapter.adapt(scheduledTask, task), initialDelay, period, unit);

        if (foliaTask != null) {
            scheduledTask.setFoliaTask(foliaTask);
            scheduledTask.start();
        }

        return scheduledTask;
    }

    @Override
    public void cancelTasks(@NotNull Plugin plugin) {
        invokeFoliaMethod(plugin, "cancelTasks", plugin);
    }
}