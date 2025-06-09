package org.astral.parkour_plugin.compatibilizer.adapters;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ActionbarApi {

    private static Method spigotSendMessageMethod = null;
    private static boolean initialized = false;

    public static void send(final Player player, final String message) {
        if (!initialized) {
            initialize(player);
        }

        if (spigotSendMessageMethod != null) {
            try {
                Object spigot = player.getClass().getMethod("spigot").invoke(player);
                Class<?> chatMessageTypeClass = Class.forName("net.md_5.bungee.api.ChatMessageType");
                Object actionBarEnum = Enum.valueOf((Class<Enum>) chatMessageTypeClass, "ACTION_BAR");

                Class<?> textComponentClass = Class.forName("net.md_5.bungee.api.chat.TextComponent");
                Object textComponent = textComponentClass.getConstructor(String.class).newInstance(message);

                spigotSendMessageMethod.invoke(spigot, actionBarEnum, textComponent);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to NMS below
            }
        }

        // Fallback for 1.8 - 1.11
        try {
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

            Class<?> chatComponentText = Class.forName("net.minecraft.server." + version + ".ChatComponentText");
            Class<?> iChatBaseComponent = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");

            Constructor<?> chatComponentConstructor = chatComponentText.getConstructor(String.class);
            Object component = chatComponentConstructor.newInstance(message);

            Class<?> packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
            Object packet = packetClass.getConstructor(iChatBaseComponent, byte.class).newInstance(component, (byte) 2);

            Method sendPacket = Class.forName("net.minecraft.server." + version + ".EntityPlayer")
                    .getMethod("playerConnection")
                    .invoke(craftPlayer)
                    .getClass()
                    .getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"));

            sendPacket.invoke(
                    craftPlayer.getClass().getField("playerConnection").get(craftPlayer),
                    packet
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static void initialize(Player player) {
        try {
            Object spigot = player.getClass().getMethod("spigot").invoke(player);
            spigotSendMessageMethod = spigot.getClass().getMethod(
                    "sendMessage",
                    Class.forName("net.md_5.bungee.api.ChatMessageType"),
                    Class.forName("net.md_5.bungee.api.chat.BaseComponent")
            );
        } catch (Exception e) {
            // Method not available on this version
            spigotSendMessageMethod = null;
        }
        initialized = true;
    }
}