package org.astral.parkour_plugin.compatibilizer.adapters;


import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ActionBarApi {

    private ActionBarApi() {}

    public static void sendActionBar(@NotNull final Player player, @NotNull final String message) {
        sendActionBar(player, '§', message);
    }

    public static void sendActionBar(@NotNull final Player player, final char alternateChar, @NotNull final String message) {
        try {
            final Object handle = player.getClass().getMethod("getHandle").invoke(player);
            final Object connection = handle.getClass().getField("playerConnection").get(handle);

            final Object chatComponent = chatComponent(message.replace(alternateChar, '§'));

            final Class<?> packetClass = getNMSClass("PacketPlayOutChat");
            Object packet;

            try {
                // 1.12+
                final Class<?> chatMessageTypeClass = getNMSClass("ChatMessageType");
                Object gameInfo = chatMessageTypeClass.getField("GAME_INFO").get(null);
                packet = packetClass
                        .getConstructor(getNMSClass("IChatBaseComponent"), chatMessageTypeClass)
                        .newInstance(chatComponent, gameInfo);
            } catch (ClassNotFoundException | NoSuchFieldException e) {
                // 1.8 - 1.11
                packet = packetClass
                        .getConstructor(getNMSClass("IChatBaseComponent"), byte.class)
                        .newInstance(chatComponent, (byte) 2);
            }

            connection.getClass()
                    .getMethod("sendPacket", getNMSClass("Packet"))
                    .invoke(connection, packet);

        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo enviar el actionbar: " + e.getMessage(), e);
        }
    }

    private static Object chatComponent(@NotNull String text) throws Exception {
        return getNMSClass("IChatBaseComponent$ChatSerializer")
                .getMethod("a", String.class)
                .invoke(null, "{\"text\": \"" + text + "\"}");
    }

    private static String getServerVersion() {
        return org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    private static @NotNull Class<?> getNMSClass(@NotNull String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + getServerVersion() + "." + name);
    }
}