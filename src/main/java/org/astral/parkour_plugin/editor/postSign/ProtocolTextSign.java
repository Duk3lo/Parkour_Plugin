package org.astral.parkour_plugin.editor.postSign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedRegistrable;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import org.astral.parkour_plugin.compatibilizer.adapters.MaterialApi;
import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.editor.Gui;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class ProtocolTextSign implements TextSignApi, Listener {
    private final ProtocolManager protocolManager;
    private static final Map<Player, BlockPosition> originalBlockMap = new HashMap<>();
    private static final String NBT_FORMAT = "{\"text\":\"%s\"}";
    private static final int[] version = ApiCompatibility.ARRAY_VERSION();
    private static final int first = version[0];
    private static final int second = version[1];

    public ProtocolTextSign(final JavaPlugin plugin) {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (first != 1) return;
                final Player player = event.getPlayer();
                if (!Gui.isInEditMode(player)) return;
                final PacketContainer packet = event.getPacket();
                String firstLine = "";
                if (second == 8) {
                    final WrappedChatComponent[] chatComponents = packet.getChatComponentArrays().read(0);
                    final String component = chatComponents[0].getJson();
                    firstLine = component.replaceAll("^\"|\"$", "");
                } else if (second >= 9) {
                    final String[] lines = packet.getStringArrays().read(0);
                    firstLine = lines[0];
                }
                Gui.CreatedMap(player, firstLine);
                final BlockPosition signPost = originalBlockMap.get(player);
                final Location location = new Location(player.getWorld(), signPost.getX(), signPost.getY(), signPost.getZ());
                final Runnable runnable = ()->{
                    final Material m1 = location.getBlock().getType();
                    final PacketContainer originalBlock = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
                    originalBlock.getBlockPositionModifier().write(0, signPost);
                    originalBlock.getBlockData().write(0, WrappedBlockData.createData(m1));
                    protocolManager.sendServerPacket(player, originalBlock);
                };
                if (ApiCompatibility.IS_FOLIA()) Kit.getRegionScheduler().execute(plugin, location, runnable);
                else runnable.run();
                originalBlockMap.remove(player);
            }
        });
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.BLOCK_CHANGE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final Player player = event.getPlayer();
                BlockPosition position = event.getPacket().getBlockPositionModifier().read(0);
                if (originalBlockMap.containsKey(player) && originalBlockMap.get(player).equals(position)) event.setCancelled(true);
            }
        });
    }

    @Override
    public void AddNewMap(final @NotNull Player player) {
        if (first != 1) return;
        final Location location = player.getLocation();
        if (second <= 19) location.setY(255);
        else location.subtract(0,2,0);
        final BlockPosition signPost = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        final PacketContainer blockChangePacket = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        blockChangePacket.getBlockPositionModifier().write(0, signPost);
        blockChangePacket.getBlockData().write(0, WrappedBlockData.createData(MaterialApi.getMaterial("OAK_SIGN","SIGN_POST","SIGN")));
        protocolManager.sendServerPacket(player, blockChangePacket);
        originalBlockMap.put(player, signPost);
        PacketContainer updatePacket = null;
        if (second == 8){
            //noinspection deprecation
            updatePacket = protocolManager.createPacket(PacketType.Play.Server.UPDATE_SIGN);
            updatePacket.getBlockPositionModifier().write(0, signPost);
            final WrappedChatComponent[] text = new WrappedChatComponent[4];
            for (int i = 0; i < Lore.size(); i++) text[i] = WrappedChatComponent.fromText(Lore.get(i));
            updatePacket.getChatComponentArrays().write(0, text);
        }else if (second >= 9){
            updatePacket = protocolManager.createPacket(PacketType.Play.Server.TILE_ENTITY_DATA);
            updatePacket.getBlockPositionModifier().write(0, signPost);

            final String[] lines = Lore.toArray(new String[0]);
            NbtCompound compound = (NbtCompound) updatePacket.getNbtModifier().read(0);
            for (int i = 0; i < 4; i++) {
                String line = i < lines.length ? lines[i] : "";
                String formattedLine = String.format(NBT_FORMAT, color(line));
                compound.put("Text" + (i + 1), formattedLine);
            }
            if (second >= 13 && second <= 17){
                compound.put("x", signPost.getX());
                compound.put("y", signPost.getY());
                compound.put("z", signPost.getZ());
                compound.put("id", "minecraft:sign");
                updatePacket.getIntegers().write(0, 9);
                updatePacket.getNbtModifier().write(0, compound);
            }else if (second >= 18){
                updatePacket.getBlockEntityTypeModifier().write(0, WrappedRegistrable.blockEntityType("sign"));
                updatePacket.getNbtModifier().write(0, compound);
                if (second >= 20){
                    compound = NbtFactory.ofCompound("");
                    compound.put("back_text", createTextNBT("", "", "", ""));
                    compound.put("front_text", createTextNBT(lines));
                    compound.put("is_waxed", (byte) 0);
                }
            }
            updatePacket.getNbtModifier().write(0, compound);
        }
        protocolManager.sendServerPacket(player, updatePacket);

        final PacketContainer openSignEditorPacket = protocolManager.createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        openSignEditorPacket.getBlockPositionModifier().write(0, signPost);
        if (second >= 20) openSignEditorPacket.getBooleans().write(0, true);
        protocolManager.sendServerPacket(player, openSignEditorPacket);
    }

    private @NotNull NbtCompound createTextNBT(final String... lines) {
        final NbtCompound textNBT = NbtFactory.ofCompound("");
        textNBT.put("color", "black");
        textNBT.put("has_glowing_text", (byte) 0);
        final NbtList<Object> messages = NbtFactory.ofList("");
        for (int i = 0; i < 4; i++) {
            messages.add(i < lines.length ? String.format("\"%s\"", lines[i]) : "\"\"");
        }
        textNBT.put("messages", messages);
        return textNBT;
    }

    private @NotNull String color(final String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}