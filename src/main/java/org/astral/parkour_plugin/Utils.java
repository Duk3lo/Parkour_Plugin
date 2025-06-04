package org.astral.parkour_plugin;

import org.astral.parkour_plugin.config.cache.BlockCache;
import org.astral.parkour_plugin.config.cache.EntityCache;
import org.astral.parkour_plugin.config.cache.InventoryCache;
import org.astral.parkour_plugin.editor.Gui;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        ////[1.8]
        for (final Map.Entry<EntityType, List<UUID>> entry : EntityCache.getEntityCache().entrySet()) {
            final EntityType entityType = entry.getKey();
            if (entityType.equals(EntityType.ARMOR_STAND)) {
                final List<UUID> uuids = entry.getValue();
                for (final UUID uuid : uuids) {
                    for (final World world : plugin.getServer().getWorlds()) {
                        for (final Entity e : world.getEntities()) {
                            if (e.getUniqueId().equals(uuid)) {
                                Kit.getAsyncScheduler().runNow(plugin, t ->
                                        Kit.getRegionScheduler().execute(plugin, e.getLocation(), ()->{
                                            e.remove();
                                            EntityCache.removeEntityFromCache(e);
                                        })
                                );
                                break;
                            }
                        }
                    }
                }
            }
        }


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
