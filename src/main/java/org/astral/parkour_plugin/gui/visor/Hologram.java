package org.astral.parkour_plugin.gui.visor;

import org.astral.parkour_plugin.config.cache.EntityCache;
import org.astral.parkour_plugin.config.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.gui.Gui;
import org.astral.parkour_plugin.gui.tools.Tools;
import org.astral.parkour_plugin.Kit;
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


public final class Hologram implements HologramApi {
    private final JavaPlugin plugin;
    private final Listener listener;
    private final static Map<String ,List<ArmorStand>> armorStands = new HashMap<>();
    private boolean isListenerRegistered = false;

    public Hologram(final JavaPlugin plugin){
        this.plugin = plugin;
        listener = new Listener() {
            @EventHandler
            public void onPlayerInteractEntity(final @NotNull PlayerInteractAtEntityEvent event) {
                final Player player = event.getPlayer();
                final Entity entity = event.getRightClicked();

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
    public void showHolograms(final Player player, final String map) {
        final Set<Player> playersOnMap = playersViewingMap.computeIfAbsent(map, k -> new HashSet<>());
        if (!playersOnMap.contains(player)) {
            playersOnMap.add(player);
            addingHolograms(map);
        }
    }

    private void addingHolograms(final String map){
        final CheckpointConfig config = new CheckpointConfig(map);
        for (final String name : config.keys()){
            try {
                config.getCheckpoint(name);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            final Location location = config.getLocation();
            addHologram(map, name, location);
        }
    }

    @Override
    public void hideHolograms(final Player player, final String map) {
        if (playersViewingMap.containsKey(map) && playersViewingMap.get(map) != null) {
            playersViewingMap.get(map).remove(player);
            if (playersViewingMap.get(map).isEmpty()) {
                removeAllHolograms(map);
                playersViewingMap.remove(map);
            }
        }
        registerOrUnregisterListener();
    }

    private void removeAllHolograms(final String map){
        if (armorStands.containsKey(map) && armorStands.get(map) != null) {
            for (final ArmorStand armorStand : armorStands.get(map)) {
                armorStand.remove();
                EntityCache.removeEntityFromCache(armorStand);
            }
            armorStands.remove(map);
        }
    }

    @Override
    public void addHologram(final String map, final String name, final @NotNull Location location) {
        Kit.getRegionScheduler().execute(plugin, location, () ->{
            final ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
            armorStand.setCustomName(name);
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStands.computeIfAbsent(map, k -> new ArrayList<>()).add(armorStand);
            EntityCache.addEntityToCache(armorStand);
            registerOrUnregisterListener();
        });
    }

    @Override
    public void removeHologram(final String map, final String name) {
        if (armorStands.containsKey(map)) {
            final List<ArmorStand> stands = armorStands.get(map);
            stands.removeIf(armorStand -> {
                if (Objects.equals(armorStand.getCustomName(), name)) {
                    EntityCache.removeEntityFromCache(armorStand);
                    armorStand.remove();
                    return true;
                }
                return false;
            });

            if (stands.isEmpty()) {
                armorStands.remove(map);
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