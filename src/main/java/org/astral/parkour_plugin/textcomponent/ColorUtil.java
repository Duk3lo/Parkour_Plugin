package org.astral.parkour_plugin.textcomponent;

import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)<#([A-F0-9]{6})>");

    private static final Map<String, String> LEGACY_COLOR_MAP = new HashMap<String, String>() {{
        put("000000", "&0");
        put("0000AA", "&1");
        put("00AA00", "&2");
        put("00AAAA", "&3");
        put("AA0000", "&4");
        put("AA00AA", "&5");
        put("FFAA00", "&6");
        put("AAAAAA", "&7");
        put("555555", "&8");
        put("5555FF", "&9");
        put("55FF55", "&a");
        put("55FFFF", "&b");
        put("FF5555", "&c");
        put("FF55FF", "&d");
        put("FFFF55", "&e");
        put("FFFFFF", "&f");
    }};

    public static String compileColors(final String input) {
        if (input == null) return null;

        int[] version = ApiCompatibility.ARRAY_VERSION();
        boolean legacy = version[1] < 16;

        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1).toUpperCase();
            if (legacy) {
                String legacyColor = getNearestLegacyColor(hex);
                matcher.appendReplacement(buffer, legacyColor != null ? legacyColor : "");
            } else {
                StringBuilder replacement = new StringBuilder("§x");
                for (char c : hex.toCharArray()) {
                    replacement.append('§').append(c);
                }
                matcher.appendReplacement(buffer, replacement.toString());
            }
        }
        matcher.appendTail(buffer);

        return buffer.toString().replace("&", "§");
    }

    private static String getNearestLegacyColor(final @NotNull String hex) {
        int r1 = Integer.parseInt(hex.substring(0, 2), 16);
        int g1 = Integer.parseInt(hex.substring(2, 4), 16);
        int b1 = Integer.parseInt(hex.substring(4, 6), 16);

        String bestCode = null;
        double bestDistance = Double.MAX_VALUE;

        for (Map.Entry<String, String> entry : LEGACY_COLOR_MAP.entrySet()) {
            String legacyHex = entry.getKey();
            int r2 = Integer.parseInt(legacyHex.substring(0, 2), 16);
            int g2 = Integer.parseInt(legacyHex.substring(2, 4), 16);
            int b2 = Integer.parseInt(legacyHex.substring(4, 6), 16);

            double distance = Math.pow(r2 - r1, 2) + Math.pow(g2 - g1, 2) + Math.pow(b2 - b1, 2);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestCode = entry.getValue();
            }
        }

        return bestCode;
    }
}