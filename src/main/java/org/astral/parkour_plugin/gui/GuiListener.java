package org.astral.parkour_plugin.gui;

import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.astral.parkour_plugin.config.cache.BlockCache;
import org.astral.parkour_plugin.config.Config;
import org.astral.parkour_plugin.gui.tools.BooleanTools;
import org.astral.parkour_plugin.gui.tools.DynamicTools;
import org.astral.parkour_plugin.gui.tools.StateTools;
import org.astral.parkour_plugin.gui.tools.Tools;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public final class GuiListener implements Listener {

    @EventHandler
    public void onPlayerDeath(final @NotNull PlayerDeathEvent event){
        final Player player = event.getEntity();
        if (Gui.isInEditMode(player)){
            event.getDrops().clear();
        }
    }

    @EventHandler
    public void opPlayerRespawn(final @NotNull PlayerRespawnEvent event){
        final Player player = event.getPlayer();
        if (Gui.isInEditMode(player)){
            Gui.exitEditMode(player);
            Gui.enterEditMode(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(final @NotNull PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();

        final ItemStack item = event.getItem();

        if (Gui.isInEditMode(player) && item != null) {

            if (item.isSimilar(Tools.EXIT_ITEM.getItem())) Gui.exitEditMode(player);

            if (block != null && (block.getType().name().contains("DOOR") || block.getType().name().contains("TRAP") && !block.getType().name().contains("TRAPPED")) &&event.getAction().name().contains("RIGHT")) return;

            if (event.getAction().name().contains("RIGHT")){
                if (item.isSimilar(DynamicTools.getUniquePlayerItem().getOrDefault(player, null))){
                    if (BooleanTools.SET_FLOATING_BLOCKS.getToggle() && event.getAction() == Action.RIGHT_CLICK_AIR){
                        Gui.setBlock(player, item);
                    }
                    return;
                }

                if (item.isSimilar(Tools.ADD_MAP_ITEM.getItem())){
                    if (!ApiCompatibility.HAS_PROTOCOL() && !ApiCompatibility.HAS_OPEN_SIGN() && !Gui.tempBlock.containsKey(player)) return;
                    else Gui.addMap(player);
                }
                if (block != null && item.isSimilar(Tools.CHECKPOINT_MARKER.getItem())){
                    Gui.removeCheckpoint(player, block.getLocation());
                }
            }

            if (event.getAction().name().contains("LEFT")){
                if (block != null && item.isSimilar(Tools.CHECKPOINT_MARKER.getItem())){
                    Gui.addCheckpoint(player, block.getLocation());
                }
            }

            if (item.isSimilar(Tools.REMOVE_MAP.getItem())){
                Gui.removeMap(player);
            }


            final String nameMap = Gui.getMapPlayer(player);
            if (isDynamicToolCheckpoints(item, nameMap)) {
                Gui.goToCheckpoint(player, item);
            }

            if (item.isSimilar(Tools.REORDER_CHECKPOINTS.getItem()))
                Gui.reorderCheckpoints(player);

            if (item.isSimilar(Tools.BACK_ITEM.getItem())) {
                Gui.backInventory(player);
            }

            if (item.isSimilar(Tools.NEXT_PAGE_ITEM.getItem()))
                Gui.nextPages(player);

            if (item.isSimilar(Tools.PREVIOUS_PAGE_ITEM.getItem()))
                Gui.previousPages(player);

            if (isDynamicToolMaps(item)) {
                Gui.setMapPlayer(player, DynamicTools.getName(item));
                Gui.loadEditInventoryMap(player);
            }

            if (item.isSimilar(Tools.EDIT_FEATHER_ITEM.getItem()))
                Gui.loadEditInventoryMap(player);

            if (item.isSimilar(Tools.CHANGE_ITEM_POSITION.getItem()))
                Gui.changeItems(player);

            if (item.isSimilar(Tools.OPEN_INVENTORY_ITEM.getItem()))
                Gui.openInventoryOptions(player);

            if (item.isSimilar(Tools.ONE_CHECKPOINT_MENU.getItem()))
                Gui.loadOneCheckpointMap(player);

            if (item.isSimilar(Tools.SPAWN_AND_FINISH_MENU.getItem()))
                Gui.loadSpawnMenu(player);

            if (isCustomTool(item) || isBooleanTool(item) || isDynamicToolMaps(item) || isDynamicToolCheckpoints(item, nameMap) || isStateTool(item))event.setCancelled(true);


            player.updateInventory();
        }else if (Gui.tempBlock.containsValue(block)) event.setCancelled(true);

        if (item != null && item.isSimilar(Tools.GENERATION_BLOCK.getItem())) {
            if (event.getAction().name().contains("LEFT")) {
                player.sendMessage("¡Usaste el Block Generator con Click Izquierdo!");
            } else if (event.getAction().name().contains("RIGHT")) {
                player.sendMessage("¡Usaste el Block Generator con Click Derecho!");
            }
        }
    }

    @EventHandler
    public void onBookEdit(final @NotNull PlayerEditBookEvent event) {
        final Player player = event.getPlayer();
        if (Gui.isInEditMode(player)) event.setCancelled(true);
    }


    @EventHandler
    public void onItemPickup(final @SuppressWarnings("deprecation") @NotNull PlayerPickupItemEvent event){
        final Player player = event.getPlayer();
        if (Gui.isInEditMode(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClickEvent(final @NotNull InventoryClickEvent event) {
        final ItemStack item = event.getCurrentItem();
        final Inventory clickedInventory = event.getClickedInventory();

        final String inventoryName = event.getView().getTitle();
        final Player player = ((Player) event.getWhoClicked()).getPlayer();

        if (Gui.isInEditMode(player)){

            if (item != null) {
                isBooleanTool(item);
                isStateTool(item);
                if (item.isSimilar(Tools.REMOVE_MAP.getItem())){
                    Gui.removeMap(player);
                }
            }

            if (inventoryName.equals(Gui.order)) {
                final Inventory topInventory = event.getView().getTopInventory();
                if (clickedInventory != null && clickedInventory.equals(topInventory)) {
                    if (event.getAction().equals(InventoryAction.PLACE_ONE)) event.setCancelled(true);
                } else {
                    event.setCancelled(true);
                }
                if ((event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD) || event.getAction().equals(InventoryAction.HOTBAR_SWAP) || event.getAction().equals(InventoryAction.PICKUP_HALF) || event.getAction().equals(InventoryAction.DROP_ALL_CURSOR)))
                    event.setCancelled(true);

                if (item != null) {
                    if (item.isSimilar(Tools.CANCEL_CHANGES_ITEM.getItem())) {
                        player.getItemOnCursor();
                        player.setItemOnCursor(null);
                        Gui.cancelChangesTop(player);
                        event.setCancelled(true);
                    }
                    if (item.isSimilar(Tools.APPLY_CHANGES_ITEM.getItem())) {
                        Gui.applyChangesCheckpoints(player);
                        event.setCancelled(true);
                    }
                    if (item.isSimilar(Tools.BACK_ITEM.getItem())) {
                        player.closeInventory();
                        event.setCancelled(true);
                    }
                    if (item.isSimilar(Tools.EXIT_ITEM.getItem())) {
                        player.closeInventory();
                        Gui.exitEditMode(player);
                        event.setCancelled(true);
                    }
                } else event.setCancelled(true);
            }else event.setCancelled(true);

        }
    }

    @EventHandler
    public void onInventoryDrag(final @NotNull InventoryDragEvent event) {
        final String inventoryName = event.getView().getTitle();
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (Gui.isInEditMode(player)) {
                if (inventoryName.equals(Gui.order)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final @NotNull InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            final Player player = (Player) event.getPlayer();
            if (Gui.isInEditMode(player)) {
                player.getItemOnCursor();
                player.setItemOnCursor(null);
            }
        }
    }

    @EventHandler
    public void onItemClone(final @NotNull InventoryCreativeEvent event) {
        if (event.getClick() == ClickType.CREATIVE) {
            final Player player = (Player) event.getWhoClicked();
            if (Gui.isInEditMode(player)){
                final ItemStack itemStack = event.getCursor();
                if (itemStack.getType() != Material.AIR) {
                    if (BooleanTools.COPY_BLOCKS_EDIT_MODE.getToggle()){
                        DynamicTools.setUniquePlayerItem(player, itemStack);
                        Gui.setItemModifiable(player);
                    }
                    else {
                        player.sendMessage("Copiar Bloques Esta desabilitado");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlaced(final @NotNull BlockPlaceEvent event){
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final ItemStack mainItem = event.getItemInHand();
        if (mainItem != null && (Gui.isInEditMode(player) && mainItem.isSimilar(Tools.ADD_MAP_ITEM.getItem()))){
            Gui.tempBlock.put(player, block);
            BlockCache.createOrUpdateOneBlockCache(player.getUniqueId(), block.getType(), block.getLocation());
            player.sendMessage("Escribe el Nombre de Tu Mapa en la Primera Linea del Cartel.");
        }
    }

    @EventHandler
    public void onBlockBreak(final @NotNull BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (Gui.isInEditMode(player)){
            if (!BooleanTools.DESTROY_BLOCKS_EDIT_MODE.getToggle()){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignChange(final @NotNull SignChangeEvent event) {
        final Player player = event.getPlayer();
        final Sign sign = (Sign) event.getBlock().getState();
        if (Gui.isInEditMode(player)) {
            final String mapName = event.getLine(0);
            Gui.updateSignMap(player, mapName, sign);

        }
    }

    @EventHandler
    public void onPlayerDropItem(final @NotNull PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (Gui.isInEditMode(player)){
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onPlayerQuit(final @NotNull PlayerQuitEvent event){
        final Player player = event.getPlayer();
        if (Gui.isInEditMode(player)){
            Gui.exitEditMode(player);
        }
    }

    @EventHandler
    public void onCommand(final @NotNull PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final String command = event.getMessage().toLowerCase();
        if (Gui.isInEditMode(player)){
            if (!Config.getCommands()) {
                player.sendMessage("Los comandos estan desabilitados");
                event.setCancelled(true);
            }else {
                if (command.contains("/clear")) {
                    event.setCancelled(true);
                    player.sendMessage("este comando esta desabilitado en el modo edicion");
                }
            }
        }
    }

    public static boolean isDynamicToolMaps(final ItemStack item) {
        return DynamicTools.SELECTS_MAPS_ITEMS.stream()
                .anyMatch(mapItem -> mapItem.isSimilar(item));
    }

    public static boolean isDynamicToolCheckpoints(final ItemStack item, final String name){
        final List<ItemStack> checkpointItems = DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name);
        if (checkpointItems == null) return false;

        return checkpointItems.stream()
                .anyMatch(checkpointItem -> checkpointItem.isSimilar(item));
    }

    public static boolean isStateTool(final ItemStack item){
        return Stream.of(StateTools.values())
                .filter(tool -> item.isSimilar(tool.getItem()))
                .findFirst()
                .map(tool -> {
                    Gui.changeStates(tool);
                    return true;
                })
                .orElse(false);
    }

    public static boolean isBooleanTool(final ItemStack item) {
        return Stream.of(BooleanTools.values())
                .filter(tool -> item.isSimilar(tool.getItem()))
                .findFirst()
                .map(tool -> {
                    Gui.updateToggles(tool);
                    return true;
                })
                .orElse(false);
    }

    public static boolean isCustomTool(final ItemStack item) {
        return Stream.of(Tools.values())
                .map(Tools::getItem)
                .anyMatch(toolItem -> toolItem.isSimilar(item));
    }
}
