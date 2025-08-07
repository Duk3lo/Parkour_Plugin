package org.astral.parkour_plugin;

import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.config.cache.BlockCache;
import org.astral.parkour_plugin.config.cache.EntityCache;
import org.astral.parkour_plugin.config.cache.InventoryCache;
import org.astral.parkour_plugin.config.maps.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.gui.Gui;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class Utils {

    private static final Map<UUID, ItemStack[]> inventoryCache = InventoryCache.getAllPlayerInventories();

    private static final Listener listener = new Listener() {
        @EventHandler
        public void onPlayerConnect(final @NotNull PlayerJoinEvent event){
            if (inventoryCache.isEmpty()) HandlerList.unregisterAll(this);
            final Player player = event.getPlayer();
            final UUID uuid = player.getUniqueId();
            if (InventoryCache.hasInventory(uuid)){
                player.getInventory().clear();
                player.getInventory().setContents(InventoryCache.getInventory(uuid));
                InventoryCache.removeInventory(uuid);
            }
        }
    };

    public static void loadCacheAndClear(final @NotNull JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        List<Location> allLocations = new ArrayList<>();
        for (String mapName : Configuration.getMaps()) {
            Rules rules = new Rules(mapName);
            allLocations.addAll(rules.getSpawnsPoints());
            allLocations.addAll(rules.getEndPoints());
            CheckpointConfig checkpointConfig = new CheckpointConfig(mapName);
            for (final String key : checkpointConfig.keys()) {
                try {
                    checkpointConfig.getCheckpoint(key);
                    Location checkpointLocation = checkpointConfig.getLocation();
                    allLocations.add(checkpointLocation);
                } catch (IOException ex) {
                    Bukkit.getLogger().warning("No se pudo cargar el checkpoint " + key + ": " + ex.getMessage());
                }
            }
        }

        Kit.getAsyncScheduler().runNow(plugin, t -> {
            for (final Map.Entry<EntityType, List<UUID>> entry : EntityCache.getEntityCache().entrySet()) {
                for (final UUID uuid : entry.getValue()) {
                    for (Location location : allLocations) {
                        Kit.getRegionScheduler().runDelayed(plugin, location, st -> {
                            Entity entity;
                            try {
                                Method getEntityMethod = Bukkit.class.getMethod("getEntity", UUID.class);
                                entity = (Entity) getEntityMethod.invoke(null, uuid);
                                if (entity != null) {
                                    entity.remove();
                                    EntityCache.removeEntityFromCache(entity);
                                }
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                                for (Entity worldEntity : location.getWorld().getEntities()) {
                                    if (worldEntity.getUniqueId().equals(uuid)) {
                                        worldEntity.remove();
                                        EntityCache.removeEntityFromCache(worldEntity);
                                        break;
                                    }
                                }
                            }
                        }, 20L);
                    }
                }
            }
        });

        for (final Map.Entry<UUID, Map<Material[], Location>> entry : BlockCache.cacheTempBlock().entrySet()) {
            final UUID uuid = entry.getKey();
            final Map<Material[], Location> map = entry.getValue();
            for (Map.Entry<Material[], Location> blockEntry : map.entrySet()) {
                final Material[] mat = blockEntry.getKey();
                final Location loc = blockEntry.getValue();
                Kit.getAsyncScheduler().runNow(plugin, t ->
                        Kit.getRegionScheduler().execute(plugin, loc, () -> {
                            final Block block = loc.getBlock();
                            final Material material = block.getType();
                            if (mat[1].equals(material)) {
                                block.setType(mat[0]);
                            }
                            BlockCache.deleteByIdOneBlockCache(uuid);
                        })
                );
            }
        }
    }

    public static void clear() {
        Gui.tempBlock.clear();
    }
}
