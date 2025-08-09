package org.astral.parkour_plugin.compatibilizer.scheduler.Core;

import org.bukkit.plugin.Plugin;

@SuppressWarnings("UnusedReturnValue")
public interface ScheduledTask {

    @SuppressWarnings("unused")
    Plugin getOwningPlugin();

    @SuppressWarnings("unused")
    boolean isRepeatingTask();

    CancelledState cancel();

    ExecutionState getExecutionState();

    default boolean isCancelled() {
        final ExecutionState state = this.getExecutionState();
        return state == ExecutionState.CANCELLED || state == ExecutionState.CANCELLED_RUNNING;
    }

    enum ExecutionState {
        IDLE,
        RUNNING,
        FINISHED,
        CANCELLED,
        CANCELLED_RUNNING
    }

    enum CancelledState {
        CANCELLED_BY_CALLER,
        CANCELLED_ALREADY,
        //RUNNING,
        ALREADY_EXECUTED,
        NEXT_RUNS_CANCELLED,
        NEXT_RUNS_CANCELLED_ALREADY,
    }
}