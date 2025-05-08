package org.astral.parkour_plugin.compatibilizer.scheduler.Core;

import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.astral.parkour_plugin.compatibilizer.scheduler.Types.Folia.FoliaAsyncScheduler;
import org.astral.parkour_plugin.compatibilizer.scheduler.Types.Paper.PaperAsyncScheduler;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface AsyncScheduler {

    @NotNull
    ScheduledTask runNow(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task);

    @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long delay, @NotNull TimeUnit unit);

    @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long initialDelay, long period, @NotNull TimeUnit unit);

    void cancelTasks(@NotNull Plugin plugin);

    static @NotNull AsyncScheduler __API(){
        if (ApiCompatibility.IS_FOLIA()){
            return new FoliaAsyncScheduler();
        }else {
            return new PaperAsyncScheduler();
        }
    }
}
