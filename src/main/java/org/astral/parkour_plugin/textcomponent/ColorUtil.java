package org.astral.parkour_plugin.textcomponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)<#([A-F0-9]{6})>");

    public static String compileColors(String input) {
        if (input == null) return null;

        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        // Legacy codes (&a, &b, etc.)
        return buffer.toString().replace("&", "§");
    }
}
