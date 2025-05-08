package org.astral.parkour_plugin.compatibilizer.adapters;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TitleApi {
    public static void send(final Player player, final String title, final String subtitle, final int fadeIn, final int stay, final int fadeOut) {
        try {
            final Object handle = player.getClass().getMethod("getHandle").invoke(player);
            final Object connection = handle.getClass().getField("playerConnection").get(handle);

            // Title Times Packet
            final Object timesPacket = getNMSClass("PacketPlayOutTitle")
                    .getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
                            getNMSClass("IChatBaseComponent"), int.class, int.class, int.class)
                    .newInstance(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null),
                            chatComponent(title), fadeIn, stay, fadeOut);

            // Title Packet
            final Object titlePacket = getNMSClass("PacketPlayOutTitle")
                    .getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
                            getNMSClass("IChatBaseComponent"))
                    .newInstance(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null),
                            chatComponent(title));

            // Subtitle Packet
            final Object subtitlePacket = getNMSClass("PacketPlayOutTitle")
                    .getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
                            getNMSClass("IChatBaseComponent"))
                    .newInstance(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null),
                            chatComponent(subtitle));

            connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, timesPacket);
            connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, titlePacket);
            connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, subtitlePacket);

        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo enviar el título: " + e.getMessage(), e);
        }

    }

    private static Object chatComponent(String text) throws Exception {
        return getNMSClass("IChatBaseComponent$ChatSerializer")
                .getMethod("a", String.class)
                .invoke(null, "{\"text\": \"" + text + "\"}");
    }

    private static String getServerVersion() {
        return org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    private static @NotNull Class<?> getNMSClass(String name) throws ClassNotFoundException {
        String version = getServerVersion();
        return Class.forName("net.minecraft.server." + version + "." + name);
    }
}
