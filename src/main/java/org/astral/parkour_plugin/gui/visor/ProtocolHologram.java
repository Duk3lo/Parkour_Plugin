package org.astral.parkour_plugin.gui.visor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.astral.parkour_plugin.config.cache.EntityCache;
import org.astral.parkour_plugin.config.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.gui.tools.Tools;
import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.gui.Gui;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public final class ProtocolHologram implements HologramApi {

    private final JavaPlugin plugin;
    private final ProtocolManager protocolManager;
    private final PacketAdapter adapter;
    private final Listener listener;
    private final Map<String, List<PacketStructureClass>> protocolStands = new HashMap<>();
    private final Map<Player, Set<Integer>> visibleEntities = new HashMap<>();

    private static final double RANGE = 144;

    private static final int[] version = ApiCompatibility.ARRAY_VERSION();
    private static final int first = version[0];
    private static final int second = version[1];
    private static final int three = version[2];

    public ProtocolHologram(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        adapter = new PacketAdapter(plugin, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (first != 1) return;
                final Player player = event.getPlayer();
                if (Gui.isInEditMode(player)) {
                    boolean useArmor = false;
                    @SuppressWarnings("deprecation") final ItemStack item = player.getInventory().getItemInHand();
                    final PacketContainer packet = event.getPacket();
                    if (second >= 8 && second <= 16) {
                        final EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);
                        if (item.isSimilar(Tools.CHECKPOINT_MARKER.getItem()) && action.name().equals("INTERACT_AT")) {
                            useArmor = true;
                        }
                    } else if (second >= 17) {
                        final List<WrappedEnumEntityUseAction> actions = packet.getEnumEntityUseActions().getValues();
                        if (!actions.isEmpty()) {
                            WrappedEnumEntityUseAction actionWrapper = actions.get(0);
                            final EnumWrappers.EntityUseAction action = actionWrapper.getAction();
                            if (item.isSimilar(Tools.CHECKPOINT_MARKER.getItem()) && action == EnumWrappers.EntityUseAction.INTERACT_AT) {
                                useArmor = true;
                            }
                        }
                    }
                    if (useArmor) {
                        final int entityId = packet.getIntegers().read(0);
                        final String map = getMapOfPlayer(player);
                        if (map != null && protocolStands.containsKey(map)) {
                            PacketStructureClass entityData = protocolStands.get(map).stream()
                                    .filter(e -> e.getEntityIdPacket() == entityId)
                                    .findFirst()
                                    .orElse(null);
                            if (entityData != null) {
                                Location location = entityData.getLocation();
                                Gui.removeCheckpoint(player, location);
                            }
                        }
                    }

                }
            }
        };

        listener = new Listener() {
            @EventHandler
            public void onPlayerMove(final @NotNull PlayerMoveEvent event) {
                final Player player = event.getPlayer();
                final Location playerLocation = player.getLocation();
                final Set<Integer> visible = visibleEntities.computeIfAbsent(player, k -> new HashSet<>());
                for (final Map.Entry<String, List<PacketStructureClass>> entry : protocolStands.entrySet()) {
                    final String viewerId = entry.getKey();
                    final List<PacketStructureClass> packetList = entry.getValue();
                    final Set<Player> viewers = playersViewingMap.get(viewerId);
                    if (viewers == null || viewers.isEmpty() || !viewers.contains(player)) {
                        continue;
                    }
                    for (final PacketStructureClass packetStructure : packetList) {
                        final Location packetLocation = packetStructure.getLocation();
                        final int entityId = packetStructure.getEntityIdPacket();
                        if (!playerLocation.getWorld().equals(packetLocation.getWorld())) continue;
                        final double distanceSquared = playerLocation.distanceSquared(packetLocation);
                        if (distanceSquared <= RANGE * RANGE) {
                            if (!visible.contains(entityId)) {
                                protocolManager.sendServerPacket(player, packetStructure.getEntityPacket());
                                protocolManager.sendServerPacket(player, packetStructure.getMetadataPacket());
                                visible.add(entityId);
                            }
                        } else {
                            if (visible.contains(entityId)) {
                                PacketContainer destroyPacket = destroyPacket(new int[]{entityId});
                                protocolManager.sendServerPacket(player, destroyPacket);
                                visible.remove(entityId);
                            }
                        }
                    }
                }
            }
        };
    }

    private @Nullable String getMapOfPlayer(final Player player) {
        for (Map.Entry<String, Set<Player>> entry : playersViewingMap.entrySet()) {
            if (entry.getValue().contains(player)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void registerOrUnregisterListener() {
        if (!plugin.isEnabled()) return;
        if (!(version.length >= 2)) return;
        boolean hasAnyPacket = protocolStands.values().stream()
                .anyMatch(list -> !list.isEmpty());
        if (hasAnyPacket) {
            if (protocolManager.getPacketListeners().contains(adapter)) {
                return;
            }
            protocolManager.addPacketListener(adapter);
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        } else {
            protocolManager.removePacketListener(adapter);
            HandlerList.unregisterAll(listener);
        }
    }

    @Override
    public void showHolograms(final Player player, final String map) {
        final Set<Player> playersOnMap = playersViewingMap.computeIfAbsent(map, k -> new HashSet<>());
        if (!playersOnMap.contains(player)) {
            playersOnMap.add(player);
            if (protocolStands.containsKey(map)) {
                final List<PacketStructureClass> packetsClass = protocolStands.get(map);
                for (PacketStructureClass packetStructureClass : packetsClass) {
                    final PacketContainer entity = packetStructureClass.getEntityPacket();
                    protocolManager.sendServerPacket(player, entity);
                    final PacketContainer metadata = packetStructureClass.getMetadataPacket();
                    protocolManager.sendServerPacket(player, metadata);
                }
            } else addingHolograms(map);
        }
    }

    private void addingHolograms(final String map) {
        final CheckpointConfig config = new CheckpointConfig(map);
        for (final String name : config.keys()) {
            try {
                config.getCheckpoint(name);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to load checkpoint '" + name + "' for map '" + map + "': " + e.getMessage());
                continue;
            }
            final Location location = config.getLocation();
            addHologram(map, name, location);
        }
    }

    private void createArmorStandProtocol(final Player player, final @NotNull Location location, final String map, final String name, final WrappedDataWatcher watcher) {
        if (first != 1) return;
        PacketContainer packet1 = null;
        final int entityId = (int) (Math.random() * Integer.MAX_VALUE);
        final UUID uuid = UUID.randomUUID();
        final double x = location.getX();
        final double y = location.getY();
        final double z = location.getZ();
        @SuppressWarnings("deprecation") final int idArmor = EntityType.ARMOR_STAND.getTypeId();

        //**Versión 1.8 - 1.12** -> SPAWN_ENTITY_LIVING
        if (second >= 8 && second <= 12) {
            //noinspection deprecation
            packet1 = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
            packet1.getIntegers().write(0, entityId).write(1, idArmor);

            if (second == 8) {
                packet1.getIntegers()
                        .write(2, (int) (x * 32D))
                        .write(3, (int) (y * 32D))
                        .write(4, (int) (z * 32D));
            } else {
                packet1.getUUIDs().write(0, uuid);
                packet1.getDoubles().write(0, x).write(1, y).write(2, z);
            }
        }
        //**Versión 1.13 - 1.21+** -> SPAWN_ENTITY
        else if (second >= 13) {
            packet1 = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
            packet1.getIntegers().write(0, entityId);
            packet1.getUUIDs().write(0, uuid);
            packet1.getDoubles().write(0, x).write(1, y).write(2, z);

            if (second == 13) {
                packet1.getIntegers().write(6, 78);
            } else {
                packet1.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
            }
        }
        if (packet1 != null) {
            protocolManager.sendServerPacket(player, packet1);
        }

        //**Versión 1.8 - 1.21+** ->Metadata
        final PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet2.getIntegers().write(0, entityId);
        if (second >= 8 && second <= 18) {
            packet2.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        } else if (second >= 19) {
            final WrappedDataWatcher.Serializer chatSerializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true);
            if (chatSerializer == null)
                throw new IllegalStateException("No se pudo obtener el serializador de componentes de chat.");
            final WrappedDataWatcher.WrappedDataWatcherObject optChatFieldWatcher = new WrappedDataWatcher.WrappedDataWatcherObject(2, chatSerializer);
            final Optional<Object> optChatField = Optional.of(WrappedChatComponent.fromChatMessage(name)[0].getHandle());
            watcher.setObject(optChatFieldWatcher, optChatField);
            if ((three >= 5 && three <= 6) || second >= 21) {
                final WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);
                if (booleanSerializer == null)
                    throw new IllegalStateException("No se pudo obtener el serializador para Boolean.");
                final WrappedDataWatcher.WrappedDataWatcherObject markerWatcher = new WrappedDataWatcher.WrappedDataWatcherObject(3, booleanSerializer);
                watcher.setObject(markerWatcher, true);
            } else if (three >= 0 && three <= 4) {
                watcher.setObject(3, true);
            }
            final List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
            for (final WrappedWatchableObject entry : watcher.getWatchableObjects()) {
                if (entry == null) continue;
                final WrappedDataWatcher.WrappedDataWatcherObject watcherObject = entry.getWatcherObject();
                final WrappedDataWatcher.Serializer serializer = watcherObject.getSerializer();
                if (serializer == null)
                    throw new IllegalStateException("El serializador para el índice " + watcherObject.getIndex() + " es null.");
                wrappedDataValueList.add(
                        new WrappedDataValue(
                                watcherObject.getIndex(),
                                serializer,
                                entry.getRawValue()
                        )
                );
            }
            packet2.getDataValueCollectionModifier().write(0, wrappedDataValueList);
            protocolManager.sendServerPacket(player, packet2);
        }
        protocolStands.computeIfAbsent(map, k -> new ArrayList<>()).add(new PacketStructureClass(entityId, name, location.subtract(0.5, 0, 0.5), packet1, packet2));

    }

    private PacketContainer destroyPacket(final int[] entityId) {
        PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        if (first != 1) return destroyPacket;
        if (second >= 8 && second <= 16) {
            destroyPacket.getIntegerArrays().write(0, entityId);
        } else if (second >= 17) {
            final List<Integer> entityIdList = new ArrayList<>();
            for (int id : entityId) entityIdList.add(id);
            destroyPacket.getIntLists().write(0, entityIdList);
        }
        return destroyPacket;
    }

    @Override
    public void hideHolograms(final Player player, final String map) {
        final int[] ids = protocolStands.getOrDefault(map, new ArrayList<>()).stream()
                .mapToInt(PacketStructureClass::getEntityIdPacket)
                .toArray();
        final PacketContainer destroyPacket = destroyPacket(ids);
        protocolManager.sendServerPacket(player, destroyPacket);
        if (playersViewingMap.containsKey(map) && playersViewingMap.get(map) != null) {
            playersViewingMap.get(map).remove(player);
            if (playersViewingMap.get(map).isEmpty()) {
                playersViewingMap.remove(map);
            }
        }
        registerOrUnregisterListener();
    }

    private void removeAllHolograms(final String map) {
        if (protocolStands.containsKey(map) && protocolStands.get(map) != null) {
            final int[] ids = protocolStands.get(map).stream()
                    .mapToInt(PacketStructureClass::getEntityIdPacket)
                    .toArray();
            final PacketContainer destroyPacket = destroyPacket(ids);
            for (Player player : playersViewingMap.getOrDefault(map, new HashSet<>())) {
                protocolManager.sendServerPacket(player, destroyPacket);
            }
            protocolStands.remove(map);
        }
    }

    @Override
    public void addHologram(final String map, final String name, final @NotNull Location location) {
        if (first != 1) return;
        Kit.getRegionScheduler().execute(plugin, location, ()->{
            final Location localCtl = location.clone();
            localCtl.setY(CheckpointConfig.MAX_Y);
            ArmorStand armorStand = location.getWorld().spawn(localCtl, ArmorStand.class);
            armorStand.setCustomName(name);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            EntityCache.addEntityToCache(armorStand);
            WrappedDataWatcher watcher = null;
            if (second >= 8 && second <= 18) {
                armorStand.setCustomNameVisible(true);
                watcher = WrappedDataWatcher.getEntityWatcher(armorStand);
            }else if (second >= 19){
                watcher = WrappedDataWatcher.getEntityWatcher(armorStand).deepClone();
            }
            for (Player viewer : playersViewingMap.getOrDefault(map, new HashSet<>())) {
                createArmorStandProtocol(viewer, location, map, name, watcher);
            }
            armorStand.remove();
            EntityCache.removeEntityFromCache(armorStand);
            registerOrUnregisterListener();
        });
    }

    @Override
    public void removeHologram(final String map, final String name) {
        if (protocolStands.containsKey(map)) {
            final List<PacketStructureClass> stands = protocolStands.get(map);
            stands.removeIf(packetStructureClass -> {
                if (Objects.equals(packetStructureClass.getName(), name)) {
                    final PacketContainer destroyPacket = destroyPacket(new int[]{packetStructureClass.getEntityIdPacket()});
                    for (Player viewer : playersViewingMap.getOrDefault(map, new HashSet<>())) {
                        protocolManager.sendServerPacket(viewer, destroyPacket);
                    }
                    return true;
                }
                return false;
            });
            if (stands.isEmpty()) {
                protocolStands.remove(map);
            }
        }
        registerOrUnregisterListener();
    }

    @Override
    public void reorderArmorStandNames(final String map) {
        removeAllHolograms(map);
        addingHolograms(map);
        registerOrUnregisterListener();
    }
}