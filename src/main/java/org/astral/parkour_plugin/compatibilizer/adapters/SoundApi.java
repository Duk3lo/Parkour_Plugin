package org.astral.parkour_plugin.compatibilizer.adapters;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class SoundApi {

    public static void playSound(final @NotNull Player player, float volume, float pitch, final String @NotNull ... soundNames) {
        Sound sound = getSound(soundNames);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private static Sound getSound(String @NotNull ... soundNames) {
        for (String soundName : soundNames) {
            try {
                return Sound.valueOf(soundName);
            } catch (IllegalArgumentException ignored) {
            }
        }
        throw new IllegalArgumentException("No se pudo encontrar un sonido válido con los nombres proporcionados: " + Arrays.toString(soundNames));
    }
}