package org.astral.parkour_plugin.compatibilizer.adapters;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.reflect.Method;

public class WorldHeightUtils {

    public static int getMaxHeight(World world) {
        try {
            Method getMaxHeightMethod = World.class.getMethod("getMaxHeight");
            return (int) getMaxHeightMethod.invoke(world);
        } catch (Exception e) {
            return isLegacyVersion() ? 256 : 319;
        }
    }

    public static int getMinHeight(World world) {
        try {
            Method getMinHeightMethod = World.class.getMethod("getMinHeight");
            return (int) getMinHeightMethod.invoke(world);
        } catch (Exception e) {
            return isLegacyVersion() ? 0 : -64;
        }
    }

    private static boolean isLegacyVersion() {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        version = version.substring(version.lastIndexOf('.') + 1);
        return version.startsWith("v1_8") || version.startsWith("v1_9") || version.startsWith("v1_10") ||
                version.startsWith("v1_11") || version.startsWith("v1_12");
    }
}