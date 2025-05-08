package org.astral.parkour_plugin;

import org.jetbrains.annotations.NotNull;

public final class Kit {

    private Kit() {}

    public static @NotNull org.astral.parkour_plugin.compatibilizer.scheduler.Core.RegionScheduler getRegionScheduler() {
        return org.astral.parkour_plugin.compatibilizer.scheduler.Core.RegionScheduler.__API();
    }

    public static @NotNull org.astral.parkour_plugin.compatibilizer.scheduler.Core.AsyncScheduler getAsyncScheduler() {
        return org.astral.parkour_plugin.compatibilizer.scheduler.Core.AsyncScheduler.__API();
    }
}