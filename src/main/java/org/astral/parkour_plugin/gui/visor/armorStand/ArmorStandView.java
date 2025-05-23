package org.astral.parkour_plugin.gui.visor.armorStand;

import org.astral.parkour_plugin.config.cache.EntityCache;
import org.astral.parkour_plugin.config.maps.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.gui.Gui;
import org.astral.parkour_plugin.gui.tools.Tools;
import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.gui.visor.Type;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;


public final class ArmorStandView implements ArmorStandApi {
    private final JavaPlugin plugin;
    private final Listener listener;
    private final static Map<String ,Map<Type, List<ArmorStand>>> armorStands = new HashMap<>();
    private boolean isListenerRegistered = false;

    public ArmorStandView(final JavaPlugin plugin){
        this.plugin = plugin;
        listener = new Listener() {
            @EventHandler
            public void onPlayerInteractEntity(final @NotNull PlayerInteractAtEntityEvent event) {
                final Player player = event.getPlayer();
                final Entity entity = event.getRightClicked();
                System.out.println("interact");

                if (Gui.isEntityArmorStandOfGUI(entity.getUniqueId())) event.setCancelled(true);

                if (Gui.isInEditMode(player)){
                    @SuppressWarnings("deprecation")
                    final ItemStack item = player.getInventory().getItemInHand();

                    if (entity instanceof ArmorStand) {
                        final ArmorStand armorStand = (ArmorStand) entity;
                        if (item.isSimilar(Tools.CHECKPOINT_MARKER.getItem())) {
                            Gui.removeCheckpoint(player, armorStand.getLocation().subtract(0.5, 0, 0.5));
                        }
                    }
                    event.setCancelled(true);
                }
            }
        };
    }

    private void registerOrUnregisterListener() {
        if (!plugin.isEnabled()) return;

        boolean hasAnyHologram = armorStands.values().stream()
                .flatMap(typeMap -> typeMap.values().stream())
                .anyMatch(list -> !list.isEmpty());

        if (hasAnyHologram) {
            if (!isListenerRegistered) {
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
                isListenerRegistered = true;
            }
        } else {
            if (isListenerRegistered) {
                HandlerList.unregisterAll(listener);
                isListenerRegistered = false;

            }
        }
    }

    @Override
    public void showHolograms(final Player player, final String map, final Type type) {
        playersViewingMap.computeIfAbsent(map, k -> new HashSet<>()).add(player);
        addingHolograms(map, type);
    }

    private void addingHolograms(final String map, final @NotNull Type type){

        

        switch (type) {
            case CHECKPOINT:
                if (armorStands.containsKey(map) && armorStands.get(map).containsKey(Type.CHECKPOINT)) break;
                final CheckpointConfig checkpoint = new CheckpointConfig(map);
                for (final String name : checkpoint.keys()) {
                    try {
                        checkpoint.getCheckpoint(name);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    final Location location = checkpoint.getLocation();
                    addHologram(map, name, location, type);
                }
                break;
            case SPAWN:
                if (armorStands.containsKey(map) && armorStands.get(map).containsKey(Type.SPAWN)) break;
                final Rules rulesSpawn = new Rules(map);
                for (final String key : rulesSpawn.getSpawnKeys()) {
                    final Location location = rulesSpawn.getSpawnLocationFromKey(key);
                    if (location == null) continue;
                    addHologram(map, key, location, type);
                }
                break;
            case END_POINT:
                if (armorStands.containsKey(map) && armorStands.get(map).containsKey(Type.END_POINT)) break;
                final Rules rulesEnd = new Rules(map);
                for (final String key : rulesEnd.getEndKeys()) {
                    final Location location = rulesEnd.getSpawnLocationFromKey(key);
                    if (location == null) continue;
                    addHologram(map, key, location, type);
                }
                break;
        }
    }

    @Override
    public void hideHolograms(final Player player, final String map, final Type type) {
        if (playersViewingMap.containsKey(map) && playersViewingMap.get(map) != null) {
            playersViewingMap.get(map).remove(player);
            if (playersViewingMap.get(map).isEmpty()) {
                removeAllHolograms(map, type);
                playersViewingMap.remove(map);
            }
        }
        registerOrUnregisterListener();
    }

    private void removeAllHolograms(final String map, final Type type) {
        final Map<Type, List<ArmorStand>> armorType = armorStands.get(map);
        if (armorType != null && armorType.containsKey(type)) {
            for (final ArmorStand armorStand : armorType.get(type)) {
                armorStand.remove();
                EntityCache.removeEntityFromCache(armorStand);
            }
            armorType.remove(type);
            if (armorType.values().stream().allMatch(List::isEmpty)) {
                armorStands.remove(map);
            }
        }
    }

    @Override
    public void addHologram(final String map, final String name, final @NotNull Location location, final Type type) {
        Kit.getRegionScheduler().execute(plugin, location, () ->{
            final ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
            armorStand.setCustomName(name);
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);
            armorStand.setVisible(false);

            armorStands
                    .computeIfAbsent(map, k -> new HashMap<>())
                    .computeIfAbsent(type, k -> new ArrayList<>())
                    .add(armorStand);
            EntityCache.addEntityToCache(armorStand);
            registerOrUnregisterListener();
        });
    }

    @Override
    public void removeHologram(final String map, final String name, final Type type) {
        final Map<Type, List<ArmorStand>> armorType = armorStands.get(map);
        if (armorType != null) {
            final List<ArmorStand> stands = armorType.get(type);
            if (stands != null) {
                stands.removeIf(armorStand -> {
                    if (Objects.equals(armorStand.getCustomName(), name)) {
                        EntityCache.removeEntityFromCache(armorStand);
                        armorStand.remove();
                        return true;
                    }
                    return false;
                });

                if (stands.isEmpty()) {
                    armorType.remove(type);
                }
            }
            if (armorType.isEmpty()) {
                armorStands.remove(map);
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