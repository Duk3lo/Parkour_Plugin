package org.astral.parkour_plugin.compatibilizer.scheduler.Core;

import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.astral.parkour_plugin.compatibilizer.scheduler.Types.Folia.FoliaRegionScheduler;
import org.astral.parkour_plugin.compatibilizer.scheduler.Types.Paper.PaperRegionScheduler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface RegionScheduler {

    void execute(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Runnable run);

    default void execute(@NotNull Plugin plugin, @NotNull Location location, @NotNull Runnable run) {
        this.execute(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, run);
    }

    ScheduledTask run(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> task);

    @SuppressWarnings("unused")
    default ScheduledTask run(@NotNull Plugin plugin, @NotNull Location location, @NotNull Consumer<ScheduledTask> task){
        return this.run(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task);
    }

    @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> task, long delayTicks);

    @SuppressWarnings("UnusedReturnValue")
    default @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Location location, @NotNull Consumer<ScheduledTask> task, long delayTicks) {
        return this.runDelayed(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task, delayTicks);
    }

    @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> task, long initialDelayTicks, long periodTicks);

    @SuppressWarnings("unused")
    default @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Location location, @NotNull Consumer<ScheduledTask> task, long initialDelayTicks, long periodTicks) {
        return this.runAtFixedRate(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task, initialDelayTicks, periodTicks);
    }

    static @NotNull RegionScheduler __API(){
        if (ApiCompatibility.IS_FOLIA()){
            return new FoliaRegionScheduler();
        }else {
            return new PaperRegionScheduler();
        }
    }
}