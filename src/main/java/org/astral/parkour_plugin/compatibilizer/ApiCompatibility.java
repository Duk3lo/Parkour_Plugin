package org.astral.parkour_plugin.compatibilizer;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ApiCompatibility {

    private static final String VERSION;
    private static final boolean FOLIA;
    private static final boolean OFF_HAND_METHOD;
    private static final boolean HAS_PROTOCOL;
    private static final boolean HAS_OPEN_SIGN;
    private static final int[] ARRAY_VERSION;

    static {
        VERSION = version();
        ARRAY_VERSION = array_version();
        FOLIA = FOLIA();
        OFF_HAND_METHOD = hasOffHandMethod();
        HAS_PROTOCOL = hasProtocolLib();
        HAS_OPEN_SIGN = containsMethodOpenSign();
    }

    private static @NotNull String version() {
        final String fullVersion = Bukkit.getServer().getVersion();
        final Matcher matcher = Pattern.compile("(\\d+\\.\\d+(?:\\.\\d+)?)").matcher(fullVersion);
        if (matcher.find()) return matcher.group(1);
        return "";
    }

    private static int @NotNull [] array_version() {
        final String versionString = VERSION();
        if (versionString.isEmpty()) return new int[]{-1, -1, -1};
        final String[] parts = versionString.split("\\.");
        final int[] versionInts = new int[]{0, 0, 0};
        try {
            for (int i = 0; i < parts.length; i++) versionInts[i] = Integer.parseInt(parts[i]);
        } catch (NumberFormatException e) {
            return new int[]{-1, -1, -1};
        }
        return versionInts;
    }

    private static boolean FOLIA(){
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean hasOffHandMethod() {
        try {
            Class<?> playerClass = Class.forName("org.bukkit.inventory");
            playerClass.getMethod("getItemInOffHand");
            return true;
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean hasProtocolLib(){
        try {
            Class.forName("com.comphenix.protocol.ProtocolLibrary");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean containsMethodOpenSign() {
        try {
            //noinspection JavaReflectionMemberAccess
            Player.class.getDeclaredMethod("openSign", Sign.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static String VERSION(){ return VERSION; }
    public static int[] ARRAY_VERSION(){ return ARRAY_VERSION; }

    public static boolean IS_FOLIA(){ return FOLIA; }
    public static boolean HAS_OFF_HAND_METHOD(){ return OFF_HAND_METHOD; }
    public static boolean HAS_PROTOCOL(){ return HAS_PROTOCOL; }
    public static boolean HAS_OPEN_SIGN(){ return HAS_OPEN_SIGN; }
}