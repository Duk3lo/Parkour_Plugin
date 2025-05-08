package org.astral.parkour_plugin.compatibilizer.scheduler.Types;

import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class TaskAdapter {
    @Contract(pure = true)
    public static @NotNull Consumer<Object> adapt(ScheduledTask scheduledTask, Consumer<ScheduledTask> task) {
        return paperTask -> {
            if (!scheduledTask.isCancelled()) {
                task.accept(scheduledTask);
            }
        };
    }
}