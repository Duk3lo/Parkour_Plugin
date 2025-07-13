package org.astral.parkour_plugin.views.tag_name;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.astral.parkour_plugin.config.cache.EntityCache;
import org.astral.parkour_plugin.config.maps.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.gui.editor.tools.Tools;
import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.gui.Gui;
import org.astral.parkour_plugin.views.Type;
import org.bukkit.Bukkit;
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

public final class ProtocolArmorStand implements ArmorStandApi {

    private final JavaPlugin plugin;
    private final ProtocolManager protocolManager;
    private final PacketAdapter adapter;
    private final Listener listener;
    private final Map<String, Map<Type, List<PacketStructureArmorStand>>> protocolStands = new HashMap<>();
    private final Map<UUID, Set<Integer>> visibleEntities = new HashMap<>();

    private static final double RANGE = 144;

    private static final int[] version = ApiCompatibility.ARRAY_VERSION();
    private static final int first = version[0];
    private static final int second = version[1];
    private static final int three = version[2];

    public ProtocolArmorStand(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        adapter = new PacketAdapter(plugin, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (first != 1) return;
                final Player player = event.getPlayer();
                final UUID playerUUID = player.getUniqueId();
                if (Gui.isInEditMode(player)) {
                    boolean useArmor = false;
                    @SuppressWarnings("deprecation") final ItemStack item = player.getInventory().getItemInHand();
                    final PacketContainer packet = event.getPacket();
                    if (second >= 8 && second <= 16) {
                        final EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);
                        if (isItemInteraction(item) && action.name().equals("INTERACT_AT")) {
                            useArmor = true;
                        }
                    } else if (second >= 17) {
                        final List<WrappedEnumEntityUseAction> actions = packet.getEnumEntityUseActions().getValues();
                        if (!actions.isEmpty()) {
                            WrappedEnumEntityUseAction actionWrapper = actions.get(0);
                            final EnumWrappers.EntityUseAction action = actionWrapper.getAction();
                            if (isItemInteraction(item) && action == EnumWrappers.EntityUseAction.INTERACT_AT) {
                                useArmor = true;
                            }
                        }
                    }
                    if (useArmor) {
                        final int entityId = packet.getIntegers().read(0);
                        final String map = getMapOfPlayer(player);
                        if (map != null && protocolStands.containsKey(map)) {

                            PacketStructureArmorStand entityData = protocolStands.getOrDefault(map, Collections.emptyMap())
                                    .values()
                                    .stream()
                                    .flatMap(List::stream)
                                    .filter(e -> e.getEntityIdPacket() == entityId)
                                    .findFirst()
                                    .orElse(null);
                            if (entityData != null) {
                                final Location location = entityData.getLocation();
                                Type type = entityData.getType();
                                switch (type) {
                                    case CHECKPOINT:
                                        Gui.removeCheckpoint(player, location);
                                        break;
                                    case SPAWN:
                                        Gui.removeSpawnPoint(player, location);
                                        break;
                                    case END_POINT:
                                        Gui.removeEndPoint(player, location);
                                        break;
                                }

                                Map<Type, List<PacketStructureArmorStand>> typeMap = protocolStands.get(map);
                                if (typeMap != null) {
                                    List<PacketStructureArmorStand> list = typeMap.get(type);
                                    if (list != null) {
                                        list.removeIf(e -> e.getEntityIdPacket() == entityId);
                                    }
                                }
                                visibleEntities.getOrDefault(playerUUID, Collections.emptySet()).remove(entityId);
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
                final UUID playerUUID = player.getUniqueId();
                final Location playerLocation = player.getLocation();
                final Set<Integer> visible = visibleEntities.computeIfAbsent(playerUUID, k -> new HashSet<>());

                for (final Map.Entry<String, Map<Type, List<PacketStructureArmorStand>>> entry : protocolStands.entrySet()) {
                    final String viewerId = entry.getKey();
                    final Map<Type, Set<UUID>> typeToViewers = playersViewingMap.get(viewerId);
                    if (typeToViewers == null) continue;

                    boolean isViewer = typeToViewers.values().stream()
                            .anyMatch(viewerSet -> viewerSet.contains(playerUUID));

                    if (!isViewer) continue;

                    final Map<Type, List<PacketStructureArmorStand>> typeMap = entry.getValue();
                    for (Map.Entry<Type, List<PacketStructureArmorStand>> typeEntry : typeMap.entrySet()) {
                        final Type type = typeEntry.getKey();
                        final List<PacketStructureArmorStand> packetList = typeEntry.getValue();

                        Set<UUID> viewers = typeToViewers.get(type);
                        if (viewers == null || !viewers.contains(playerUUID)) continue;

                        for (final PacketStructureArmorStand packetStructure : packetList) {
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
                                    PacketContainer destroyPacket = destroyEntity(new int[]{entityId});
                                    protocolManager.sendServerPacket(player, destroyPacket);
                                    visible.remove(entityId);
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    public boolean isItemInteraction(final @NotNull ItemStack item){
        return item.isSimilar(Tools.CHECKPOINT_MARKER.getItem()) || item.isSimilar(Tools.MARK_SPAWN_ITEM.getItem()) || item.isSimilar(Tools.MARK_FINISH_ITEM.getItem());
    }

    private @Nullable String getMapOfPlayer(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        for (Map.Entry<String, Map<Type, Set<UUID>>> entry : playersViewingMap.entrySet()) {
            final Map<Type, Set<UUID>> typeMap = entry.getValue();
            for (Set<UUID> uuids : typeMap.values()) {
                if (uuids.contains(uuid)) {
                    return entry.getKey();
                }
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
    public void showHolograms(final @NotNull Player player, final String map, final Type type) {
        final UUID playerUUID = player.getUniqueId();

        playersViewingMap.computeIfAbsent(map, k -> new HashMap<>())
                .computeIfAbsent(type, k -> new HashSet<>())
                .add(playerUUID);

        final Map<Type, List<PacketStructureArmorStand>> standTypeMap = protocolStands.get(map);

        if (standTypeMap != null) {
            final List<PacketStructureArmorStand> stands = standTypeMap.get(type);

            if (stands != null) {
                for (PacketStructureArmorStand packet : stands) {
                    protocolManager.sendServerPacket(player, packet.getEntityPacket());
                    protocolManager.sendServerPacket(player, packet.getMetadataPacket());
                }
            } else {
                addingHolograms(map, type);
            }
        } else {
            addingHolograms(map, type);
        }
    }

    private void addingHolograms(final String map, final @NotNull Type type) {
        final Map<Type, List<PacketStructureArmorStand>> standMap = protocolStands.get(map);
        if (standMap != null && standMap.containsKey(type)) return;
        switch (type) {
            case CHECKPOINT:
                final CheckpointConfig config = new CheckpointConfig(map);
                for (final String name : config.keys()) {
                    try {
                        config.getCheckpoint(name);
                        final Location location = config.getLocation();
                        addHologram(map, name, location, type);
                    } catch (IOException e) {
                        plugin.getLogger().severe("No se pudo cargar el checkpoint '" + name + "' en el mapa '" + map + "'");
                    }
                }
                break;
            case SPAWN:
            case END_POINT:
                final Rules rules = new Rules(map);
                final boolean isSpawn = type == Type.SPAWN;
                final String[] keys = isSpawn ? rules.getSpawnKeys() : rules.getEndKeys();
                for (final String key : keys) {
                    final Location location = isSpawn? rules.getSpawnLocationFromKey(key) : rules.getEndPointLocationFromKey(key);
                    if (location != null) {
                        addHologram(map, key, location, type);
                    }
                }
                break;
        }
    }

    @Override
    public void addHologram(final String map, final String name, final @NotNull Location location, final Type type) {
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
            createArmorStandProtocol(location, map, name, watcher, type);
            armorStand.remove();
            EntityCache.removeEntityFromCache(armorStand);
            registerOrUnregisterListener();
        });
    }

    private void createArmorStandProtocol(final @NotNull Location location, final String map, final String name, final WrappedDataWatcher watcher, final Type type) {
        if (first != 1) return;
        final int entityId = (int) (Math.random() * Integer.MAX_VALUE);
        final UUID uuid = UUID.randomUUID();
        final PacketContainer packet1 = createEntityPacket(entityId, uuid, location);
        final PacketContainer packet2 = createMetadataPacket(entityId, watcher, name);
        if (packet1 == null) return;
        protocolStands.computeIfAbsent(map, k -> new HashMap<>())
                .computeIfAbsent(type, k -> new ArrayList<>())
                .add(new PacketStructureArmorStand(entityId, name, location.subtract(0.5, 0, 0.5), packet1, packet2, type));


        for (UUID uuidPlayer : playersViewingMap.get(map).get(type)) {
            final Player viewer = Bukkit.getPlayer(uuidPlayer);
            if (viewer != null && viewer.isOnline()) {
                protocolManager.sendServerPacket(viewer, packet1);
                protocolManager.sendServerPacket(viewer, packet2);
            }
        }
    }

    private @Nullable PacketContainer createEntityPacket(int entityId, UUID uuid, @NotNull Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        @SuppressWarnings("deprecation") final int idArmor = EntityType.ARMOR_STAND.getTypeId();

        //**Versión 1.8 - 1.12** → SPAWN_ENTITY_LIVING
        if (second >= 8 && second <= 12) {
            @SuppressWarnings("deprecation") PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
            packet.getIntegers().write(0, entityId).write(1, idArmor);
            if (second == 8) {
                packet.getIntegers()
                        .write(2, (int) (x * 32D))
                        .write(3, (int) (y * 32D))
                        .write(4, (int) (z * 32D));
            } else {
                packet.getUUIDs().write(0, uuid);
                packet.getDoubles().write(0, x).write(1, y).write(2, z);
            }
            return packet;
        }

        //**Versión 1.13 - 1.21+** → SPAWN_ENTITY
        else if (second >= 13) {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
            packet.getIntegers().write(0, entityId);
            packet.getUUIDs().write(0, uuid);
            packet.getDoubles().write(0, x).write(1, y).write(2, z);
            if (second == 13) {
                packet.getIntegers().write(6, 78); // ArmorStand
            } else {
                packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
            }
            return packet;
        }
        return null;
    }

    private @NotNull PacketContainer createMetadataPacket(int entityId, WrappedDataWatcher watcher, String name) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entityId);

        //**Versión 1.8 - 1.18**
        if (second >= 8 && second <= 18) {
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        }

        //**Versión 1.19 - 1.21+**
        else if (second >= 19) {
            final WrappedDataWatcher.Serializer chatSerializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true);
            if (chatSerializer == null)
                throw new IllegalStateException("No se pudo obtener el serializador de componentes de chat.");

            final WrappedDataWatcher.WrappedDataWatcherObject optChatFieldWatcher =
                    new WrappedDataWatcher.WrappedDataWatcherObject(2, chatSerializer);
            final Optional<Object> optChatField = Optional.of(WrappedChatComponent.fromChatMessage(name)[0].getHandle());
            watcher.setObject(optChatFieldWatcher, optChatField);

            if ((three >= 5 && three <= 6) || second >= 21) {
                final WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);
                if (booleanSerializer == null)
                    throw new IllegalStateException("No se pudo obtener el serializador para Boolean.");
                final WrappedDataWatcher.WrappedDataWatcherObject markerWatcher =
                        new WrappedDataWatcher.WrappedDataWatcherObject(3, booleanSerializer);
                watcher.setObject(markerWatcher, true);
            } else if (three >= 0 && three <= 4) {
                watcher.setObject(3, true);
            }

            final List<WrappedDataValue> dataList = new ArrayList<>();
            for (final WrappedWatchableObject entry : watcher.getWatchableObjects()) {
                if (entry == null) continue;
                final WrappedDataWatcher.WrappedDataWatcherObject obj = entry.getWatcherObject();
                final WrappedDataWatcher.Serializer serializer = obj.getSerializer();
                if (serializer == null)
                    throw new IllegalStateException("El serializador para el índice " + obj.getIndex() + " es null.");
                dataList.add(new WrappedDataValue(obj.getIndex(), serializer, entry.getRawValue()));
            }

            packet.getDataValueCollectionModifier().write(0, dataList);
        }

        return packet;
    }

    private PacketContainer destroyEntity(final int[] entityId) {
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
    public void hideHolograms(final @NotNull Player player, final String map, final Type type) {
        final UUID playerUUID = player.getUniqueId();
        final Map<Type, List<PacketStructureArmorStand>> typeMap = protocolStands.get(map);
        if (typeMap != null) {
            final List<PacketStructureArmorStand> packetList = typeMap.get(type);
            if (packetList != null) {
                final int[] ids = packetList.stream()
                        .mapToInt(PacketStructureArmorStand::getEntityIdPacket)
                        .toArray();
                final PacketContainer destroyPacket = destroyEntity(ids);
                protocolManager.sendServerPacket(player, destroyPacket);
            }
        }

        final Map<Type, Set<UUID>> typeViewers = playersViewingMap.get(map);
        if (typeViewers != null) {
            final Set<UUID> playersForType = typeViewers.get(type);
            if (playersForType != null) {
                playersForType.remove(playerUUID);
                if (playersForType.isEmpty()) {
                    typeViewers.remove(type);
                }
            }

            if (typeViewers.isEmpty()) {
                playersViewingMap.remove(map);
            }
        }

        registerOrUnregisterListener();
    }

    private void removeAllHolograms(final String map, final Type type) {
        final Map<Type, List<PacketStructureArmorStand>> typeMap = protocolStands.get(map);
        if (typeMap != null && typeMap.containsKey(type)) {
            final List<PacketStructureArmorStand> packets = typeMap.get(type);

            final int[] ids = packets.stream()
                    .mapToInt(PacketStructureArmorStand::getEntityIdPacket)
                    .toArray();
            final PacketContainer destroyPacket = destroyEntity(ids);

            final Map<Type, Set<UUID>> viewersByType = playersViewingMap.get(map);
            if (viewersByType != null) {
                final Set<UUID> uuids = viewersByType.get(type);
                if (uuids != null) {
                    for (UUID uuid : uuids) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            protocolManager.sendServerPacket(player, destroyPacket);
                        }
                    }
                }
            }
            typeMap.remove(type);
            if (typeMap.isEmpty()) {
                protocolStands.remove(map);
            }
        }
    }

    @Override
    public void removeHologram(final String map, final String name, final Type type) {
        final Map<Type, List<PacketStructureArmorStand>> typeMap = protocolStands.get(map);
        if (typeMap != null && typeMap.containsKey(type)) {
            final List<PacketStructureArmorStand> stands = typeMap.get(type);
            final Iterator<PacketStructureArmorStand> iterator = stands.iterator();

            while (iterator.hasNext()) {
                final PacketStructureArmorStand stand = iterator.next();
                if (Objects.equals(stand.getName(), name)) {
                    final PacketContainer destroyPacket = destroyEntity(
                            new int[]{stand.getEntityIdPacket()}
                    );

                    final Map<Type, Set<UUID>> typeViewers = playersViewingMap.get(map);
                    if (typeViewers != null) {
                        final Set<UUID> viewers = typeViewers.get(type);
                        if (viewers != null) {
                            for (UUID uuid : viewers) {
                                Player viewer = Bukkit.getPlayer(uuid);
                                if (viewer != null && viewer.isOnline()) {
                                    protocolManager.sendServerPacket(viewer, destroyPacket);
                                }
                            }
                        }
                    }
                    iterator.remove();
                    break;
                }
            }
            if (stands.isEmpty()) {
                typeMap.remove(type);
            }
            if (typeMap.isEmpty()) {
                protocolStands.remove(map);
            }
        }
        registerOrUnregisterListener();
    }

    @Override
    public void reorderArmorStandNames(final String map, final Type type) {
        removeAllHolograms(map, type);
        addingHolograms(map, type);
        registerOrUnregisterListener();
    }
}