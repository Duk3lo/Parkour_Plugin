package org.astral.parkour_plugin.compatibilizer.scheduler.Types;

import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class SimpleScheduledTask implements ScheduledTask {
    private final Plugin plugin;
    private final boolean repeating;
    private ExecutionState state = ExecutionState.IDLE;
    private boolean cancelled = false;
    private boolean executed = false;

    private Object foliaTask;
    private int paperTaskId = -1;

    public SimpleScheduledTask(Plugin plugin, boolean repeating) {
        this.plugin = plugin;
        this.repeating = repeating;
    }

    public void setFoliaTask(Object foliaTask) {
        this.foliaTask = foliaTask;
    }

    public void setPaperTaskId(int paperTaskId) {
        this.paperTaskId = paperTaskId;
    }

    @Override
    public Plugin getOwningPlugin() {
        return plugin;
    }

    @Override
    public boolean isRepeatingTask() {
        return repeating;
    }

    @Override
    public CancelledState cancel() {
        if (cancelled) {
            return repeating ? CancelledState.NEXT_RUNS_CANCELLED_ALREADY : CancelledState.CANCELLED_ALREADY;
        }

        if (!repeating && executed) {
            return CancelledState.ALREADY_EXECUTED;
        }

        if (state == ExecutionState.RUNNING) {
            state = ExecutionState.CANCELLED_RUNNING;
        } else {
            state = ExecutionState.CANCELLED;
        }

        if (foliaTask != null) {
            try {
                Method cancelMethod = foliaTask.getClass().getDeclaredMethod("cancel");
                cancelMethod.setAccessible(true); // Puede lanzar una excepción en Java 17+ con módulos fuertes
                cancelMethod.invoke(foliaTask);
            } catch (Exception e) {
                plugin.getLogger().warning("No se pudo cancelar la tarea de Folia: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        }

        if (paperTaskId != -1) {
            Bukkit.getScheduler().cancelTask(paperTaskId);
        }

        cancelled = true;

        return repeating ? CancelledState.NEXT_RUNS_CANCELLED : CancelledState.CANCELLED_BY_CALLER;
    }

    @Override
    public ExecutionState getExecutionState() {
        return state;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void start() {
        if (!cancelled && state == ExecutionState.IDLE) {
            state = ExecutionState.RUNNING;
        }
    }

    public void finish() {
        if (!repeating) {
            executed = true;
            state = cancelled ? ExecutionState.CANCELLED_RUNNING : ExecutionState.FINISHED;
        }
    }
}