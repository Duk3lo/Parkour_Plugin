package org.astral.parkour_plugin.title;

import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.compatibilizer.scheduler.Core.ScheduledTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class AnimatedTitle {

    private final List<Title> frames;
    private final boolean repeat;
    private final int updateDelaySeconds;
    private static final Main plugin = Main.getInstance();

    public AnimatedTitle(List<Title> frames, boolean repeat, int updateDelaySeconds) {
        this.frames = frames;
        this.repeat = repeat;
        this.updateDelaySeconds = updateDelaySeconds;
    }

    public @NotNull ScheduledTask send(@NotNull Player player) {
        final AtomicInteger index = new AtomicInteger(0);
        return Kit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            int currentIndex = index.getAndIncrement();

            if (currentIndex >= frames.size()) {
                if (repeat) {
                    index.set(1);
                    currentIndex = 0;
                } else {
                    task.cancel();
                    return;
                }
            }
            Title frame = frames.get(currentIndex);
            frame.send(player);
        }, 0L, updateDelaySeconds, TimeUnit.SECONDS);
    }
}