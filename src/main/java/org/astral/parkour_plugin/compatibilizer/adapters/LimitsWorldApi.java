package org.astral.parkour_plugin.compatibilizer.adapters;

import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public final class LimitsWorldApi {

    private static final int[] version = ApiCompatibility.ARRAY_VERSION();
    private static final int first = version[0];
    private static final int second = version[1];

    private static final boolean IS_1_18_PLUS = (first == 1 && second >= 18);

    private static Method GET_MIN_HEIGHT = null;
    private static Method GET_MAX_HEIGHT = null;

    static {
        if (IS_1_18_PLUS) {
            try {
                //noinspection JavaReflectionMemberAccess
                GET_MIN_HEIGHT = World.class.getMethod("getMinHeight");
                GET_MAX_HEIGHT = World.class.getMethod("getMaxHeight");
            } catch (NoSuchMethodException ignored) {
            }
        }
    }

    public static int getMinY(@NotNull World world) {
        if (IS_1_18_PLUS && GET_MIN_HEIGHT != null) {
            try {
                return (int) GET_MIN_HEIGHT.invoke(world);
            } catch (Exception ignored) {
            }
        }
        return getDefaultMinY(world.getEnvironment());
    }

    public static int getMaxY(@NotNull World world) {
        if (IS_1_18_PLUS && GET_MAX_HEIGHT != null) {
            try {
                return (int) GET_MAX_HEIGHT.invoke(world);
            } catch (Exception ignored) {
            }
        }
        return getDefaultMaxY(world.getEnvironment());
    }

    private static int getDefaultMinY(@NotNull Environment env) {
        switch (env) {
            case NETHER:
            case NORMAL:
            case THE_END:
            default:
                return 0;
        }
    }

    private static int getDefaultMaxY(@NotNull Environment env) {
        switch (env) {
            case NETHER:
                return 128;
            case NORMAL:
            case THE_END:
            default:
                return 256;
        }
    }

}
