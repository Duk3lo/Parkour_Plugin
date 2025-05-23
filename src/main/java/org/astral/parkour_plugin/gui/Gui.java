package org.astral.parkour_plugin.gui;

import org.astral.parkour_plugin.compatibilizer.adapters.SoundApi;
import org.astral.parkour_plugin.compatibilizer.adapters.TeleportingApi;
import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.astral.parkour_plugin.config.cache.BlockCache;
import org.astral.parkour_plugin.config.cache.EntityCache;
import org.astral.parkour_plugin.config.cache.InventoryCache;
import org.astral.parkour_plugin.config.maps.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.gui.tools.BooleanTools;
import org.astral.parkour_plugin.gui.tools.DynamicTools;
import org.astral.parkour_plugin.gui.tools.StateTools;
import org.astral.parkour_plugin.gui.tools.Tools;
import org.astral.parkour_plugin.Kit;
import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.gui.compatible.ModernGuiListener;
import org.astral.parkour_plugin.gui.postSign.TextSignApi;
import org.astral.parkour_plugin.gui.visor.armorStand.ArmorStandApi;
import org.astral.parkour_plugin.gui.visor.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public final class Gui {

    private static final Main plugin = Main.getInstance();

    //----------------------------------------------------------------------------------[Index Pages]
    //-----------------------------------------------------------------------------------------------

    // MAP
    private static final byte INDEX_MAPS = 2;
    private static final byte ITEMS_PER_PAGE_MAPS = 5;

    // CHECKPOINTS
    private static final byte INDEX_CHECKPOINT = 3;
    private static final byte ITEMS_PER_PAGE_CHECKPOINT = 3;

    //Spawn & Finish
    private static final byte INDEX_SPAWN_FINISH = 3;
    private static final byte ITEMS_PER_PAGE_SPAWN_FINISH = 4;

    //----------------------------------------------------------------------------[Names Inventories]
    //-----------------------------------------------------------------------------------------------
    public static final String order = "Ordena Tus Checkpoints";
    public static final Map<Player, Block> tempBlock = new HashMap<>();

    private static final Map<Player, String> menu = new HashMap<>();

    private static final String main_Menu = "Main_Menu";
    private static final String checkpoint_menu = "Checkpoint_Menu";
    private static final String checkpoint_Menu_Edit = "Checkpoint_Menu_Edit";
    private static final String spawnAndFinishMenu = "Spawn_End_Menu";

    private static final Map<Player, ItemStack[]> playerInventories = new HashMap<>();
    private static final Map<Player, Boolean> editingPlayers = new HashMap<>();
    private static final Map<Player, Integer> playerPages = new HashMap<>();
    private static final Map<Player, String> mapPlayer = new HashMap<>();
    private static final Map<Player, Map<Integer, ItemStack>> originalInventories = new HashMap<>();

    private static final Inventory menuOptions = Bukkit.createInventory(null, 9, "Opciones");

    private static final ArmorStandApi HOLOGRAM_API = ArmorStandApi._view(plugin);
    private static final TextSignApi TEXT_SIGN_API = TextSignApi._text(plugin);

    private static final GuiListener GUI_LISTENER = new GuiListener();
    private static final ModernGuiListener MODERN_GUI_LISTENER = new ModernGuiListener();
    private static boolean isActiveListener = false;

    public static void enterEditMode(final Player player) {
        if (!editingPlayers.containsKey(player)) {
            final UUID uuid = player.getUniqueId();
            final ItemStack[] itemStacks = player.getInventory().getContents();
            playerInventories.put(player, itemStacks);
            InventoryCache.saveInventory(uuid, itemStacks);
            loadMainInventory(player);
            editingPlayers.put(player, true);
            SoundApi.playSound(player, 1.0f, 2.0f, "ORB_PICKUP","ENTITY_EXPERIENCE_ORB_PICKUP");
        }

        if (!editingPlayers.isEmpty() && !isActiveListener) {
            plugin.getServer().getPluginManager().registerEvents(GUI_LISTENER, plugin);
            if (ApiCompatibility.HAS_OFF_HAND_METHOD()) plugin.getServer().getPluginManager().registerEvents(MODERN_GUI_LISTENER, plugin);
            isActiveListener = true;
        }
    }

    public static void loadMainInventory(final @NotNull Player player) {
        menu.put(player, main_Menu);
        player.getInventory().clear();
        player.getInventory().setItem(0, Tools.ADD_MAP_ITEM.getItem());
        player.getInventory().setItem(8, Tools.EXIT_ITEM.getItem());
        playerPages.put(player, 0);
        showPage(player, 0, DynamicTools.SELECTS_MAPS_ITEMS,INDEX_MAPS, ITEMS_PER_PAGE_MAPS);
    }

    //----------------------------------------------------------------------[SPAWN AND FINISH]
    //----------------------------------------------------------------------------------------
    public static void loadSpawnAndEndMenu(final @NotNull Player player){
        menu.put(player, spawnAndFinishMenu);
        final String name_map = mapPlayer.getOrDefault(player, "");
        playerPages.put(player, 0);
        DynamicTools.loadSpawnPoints(name_map);
        DynamicTools.loadFinishPoints(name_map);
        player.getInventory().clear();
        player.getInventory().setItem(0, Tools.MARK_SPAWN_ITEM.getItem());
        player.getInventory().setItem(1, Tools.MARK_FINISH_ITEM.getItem());
        player.getInventory().setItem(8, Tools.BACK_ITEM.getItem());
        final List<ItemStack> all = new ArrayList<>(DynamicTools.SPAWN_LOCATIONS.get(name_map));
        all.addAll(DynamicTools.FINISH_LOCATION.get(name_map));
        showPage(player, 0, all, INDEX_SPAWN_FINISH, ITEMS_PER_PAGE_SPAWN_FINISH);
        HOLOGRAM_API.showHolograms(player, name_map, Type.SPAWN);
        HOLOGRAM_API.showHolograms(player, name_map, Type.END_POINT);
    }

    //----------------------------------------------------------------------------[CHECKPOINT]
    //----------------------------------------------------------------------------------------
    public static void loadOneCheckpointMap(final @NotNull Player player){
        menu.put(player, checkpoint_menu);
        final String name_map = mapPlayer.getOrDefault(player, "");
        playerPages.put(player, 0);
        player.getInventory().clear();
        showPage(player, 0, DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map), INDEX_CHECKPOINT, ITEMS_PER_PAGE_CHECKPOINT);
        player.getInventory().setItem(0, Tools.CHECKPOINT_MARKER.getItem());
        player.getInventory().setItem(1, DynamicTools.getUniquePlayerItem().getOrDefault(player, null));
        player.getInventory().setItem(7 , Tools.EDIT_FEATHER_ITEM.getItem());
        player.getInventory().setItem(8 , Tools.BACK_ITEM.getItem());
        player.getInventory().setItem(35, Tools.REMOVE_MAP.getItem());
        HOLOGRAM_API.showHolograms(player, name_map, Type.CHECKPOINT);
        SoundApi.playSound(player, 1.0f, 1.0f, "CLICK", "UI_BUTTON_CLICK");
    }

    //-------------------------------------------------------------[REPLACE-EDITOR-DELETE-ADD]
    //----------------------------------------------------------------------------------------

    public static void addMap(final @NotNull Player player){
        TEXT_SIGN_API.AddNewMap(player);
    }

    public static void removeMap(final @NotNull Player player){
        final String name_map = mapPlayer.getOrDefault(player, "");
        if (name_map.isEmpty()) return;
        Configuration.deleteMapFolder(name_map);
        removeMaps(name_map);
    }

    public static void addSpawnPoint(final Player player, final Location location) {
        final String name_map = mapPlayer.get(player);
        Rules rules = new Rules(name_map);
        if (rules.isEqualsLocation(location)){
            player.sendMessage("Esta ubicacion ya se marco como spawn");
        }else {
            HOLOGRAM_API.addHologram(name_map, rules.setSpawns(location), location, Type.SPAWN);
            updateAllSpawnEnd(name_map);
        }
    }

    public static void removeSpawnPointSpawn(final Player  player, final Location location){
        final String name_map = mapPlayer.get(player);
        Rules rules = new Rules(name_map);
        if (rules.isEqualsLocation(location)){
            HOLOGRAM_API.removeHologram(name_map, rules.getSpawnKeyFromLocation(location), Type.SPAWN);
            rules.removeSpawnPoint(location);
            updateAllSpawnEnd(name_map);
        }
    }

    public static void removeEndPoint(final Player player, final Location location){
        final String name_map = mapPlayer.get(player);
        Rules rules = new Rules(name_map);
        if (rules.isEqualsLocation(location)){
            HOLOGRAM_API.removeHologram(name_map, rules.getSpawnKeyFromLocation(location), Type.SPAWN);
            rules.removeEndPoint(location);
            updateAllSpawnEnd(name_map);
        }
    }

    public static void addCheckpoint(final Player player, final Location location){
        final String name_map = mapPlayer.get(player);
        CheckpointConfig checkpointConfig = new CheckpointConfig(name_map);
        for (final String key : checkpointConfig.keys()){
            try {
                checkpointConfig.getCheckpoint(key);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (checkpointConfig.isEqualsLocation(location)) {
                player.sendMessage("Esta Ubicacion ya esta registrada");
                return;
            }
        }
        final String checkpoint = checkpointConfig.createNextCheckpointName();
        checkpointConfig.createCheckpoint(checkpoint);
        try {
            checkpointConfig.getCheckpoint(checkpoint);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        checkpointConfig.setLocation(location);
        checkpointConfig.setMinFallY(CheckpointConfig.MIN_Y);
        checkpointConfig.setAllMaxFallY(CheckpointConfig.MAX_Y);
        HOLOGRAM_API.addHologram(name_map, checkpoint, checkpointConfig.getLocation(), Type.CHECKPOINT);
        updateCheckpoints(name_map);
    }

    public static void removeCheckpoint(final Player player, final Location location){
        final String name_map = mapPlayer.get(player);
        final CheckpointConfig checkpointConfig = new CheckpointConfig(name_map);
        for (final String key : checkpointConfig.keys()){
            try {
                checkpointConfig.getCheckpoint(key);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (checkpointConfig.isEqualsLocation(location)) {
                checkpointConfig.deleteCheckpoint(key);
                HOLOGRAM_API.removeHologram(name_map, key, Type.CHECKPOINT);
                updateCheckpoints(name_map);
                SoundApi.playSound(player, 0.3f, 1.0f, "ZOMBIE_WOOD", "ENTITY_ZOMBIE_ATTACK_DOOR_WOOD");
            }
        }
    }

    public static void reorderCheckpoints(final Player player){
        final String name_map = mapPlayer.get(player);
        CheckpointConfig checkpointConfig = new CheckpointConfig(name_map);
        checkpointConfig.reorderCheckpoints();
        HOLOGRAM_API.reorderArmorStandNames(name_map, Type.CHECKPOINT);
        updateCheckpoints(name_map);
    }

    public static void refreshAllMaps(){
        Kit.getAsyncScheduler().runNow(plugin, t ->{
            DynamicTools.refreshMaps();
            for (final Map.Entry<Player, String> entry : menu.entrySet()){
                if (entry.getValue().equals(main_Menu)) updateInventory(entry.getKey());
            }
        });
    }

    private static void removeMaps(final String name_map){
        Kit.getAsyncScheduler().runNow(plugin, t ->{
            DynamicTools.refreshMaps();
            for (final Map.Entry<Player, String> entry : mapPlayer.entrySet()){
                final String name = entry.getValue();
                final Player player = entry.getKey();
                if (name.equals(name_map)){
                    final String inventory = player.getOpenInventory().getTitle();
                    if (inventory.equals(order)) player.closeInventory();
                    HOLOGRAM_API.hideHolograms(player, name_map, Type.CHECKPOINT);
                    loadMainInventory(player);
                    SoundApi.playSound(player, 1.0f, 1.0f, "ITEM_BREAK", "ENTITY_ITEM_BREAK");
                }
            }
            DynamicTools.CHECKPOINTS_MAPS_ITEMS.remove(name_map);
        });
    }

    private static void updateCheckpoints(final String name_map){
        Kit.getAsyncScheduler().runNow(plugin, t ->{
            DynamicTools.loadCheckpointsItems(name_map);
            for (Map.Entry<Player, String> entry : mapPlayer.entrySet()) {
                final String nameMap = entry.getValue();
                final Player player = entry.getKey();
                if (name_map.equals(nameMap)) {
                    updateInventory(player);
                }
            }
        });
    }

    private static void updateAllSpawnEnd(final String name_map){
        Kit.getAsyncScheduler().runNow(plugin, t->{
            DynamicTools.loadSpawnPoints(name_map);
            DynamicTools.loadFinishPoints(name_map);
            for (Map.Entry<Player, String> entry : mapPlayer.entrySet()) {
                final String nameMap = entry.getValue();
                final Player player = entry.getKey();
                if (name_map.equals(nameMap)) {
                    updateInventory(player);
                }
            }
        });
    }

    //------------------------------------------------------------------------------------[Gui]
    //----------------------------------------------------------------------------------------
    public static void setMapPlayer(final Player player, final String name_map){
        mapPlayer.put(player, name_map);
    }

    public static void openInventoryOptions(final @NotNull Player player){
        final int s1 = 0;
        final int s2 = 1;
        final int s3 = 2;
        if (BooleanTools.DESTROY_BLOCKS_EDIT_MODE.getSlot() != s1) BooleanTools.DESTROY_BLOCKS_EDIT_MODE.setSlot(s1);
        if (BooleanTools.COPY_BLOCKS_EDIT_MODE.getSlot() != s2) BooleanTools.COPY_BLOCKS_EDIT_MODE.setSlot(s2);
        if (BooleanTools.SET_FLOATING_BLOCKS.getSlot() != s3) BooleanTools.SET_FLOATING_BLOCKS.setSlot(s3);

        BooleanTools.DESTROY_BLOCKS_EDIT_MODE.setItemSlot(menuOptions);
        BooleanTools.COPY_BLOCKS_EDIT_MODE.setItemSlot(menuOptions);
        BooleanTools.SET_FLOATING_BLOCKS.setItemSlot(menuOptions);
        player.openInventory(menuOptions);
        SoundApi.playSound(player, 1.0f, 1.0f, "CLICK", "UI_BUTTON_CLICK");
    }

    public static void setBlock(final @NotNull Player player, final @NotNull ItemStack itemStack) {
        final int distance = StateTools.DISTANCE_BLOCK.getValue();
        final Location eyeLocation = player.getEyeLocation();
        final Vector direction = eyeLocation.getDirection().normalize();
        final Location targetLocation = eyeLocation.clone().add(direction.multiply(distance)).getBlock().getLocation();

        Kit.getRegionScheduler().execute(plugin, targetLocation, () -> {
            if (targetLocation.getBlock().getType() == Material.AIR) {
                targetLocation.getBlock().setType(itemStack.getType());
            }
        });
    }

    public static void setItemModifiable(final @NotNull Player player){
        if (menu.get(player).equals(checkpoint_menu)){
            player.getInventory().setItem(1, DynamicTools.getUniquePlayerItem().getOrDefault(player, null));
            player.getInventory().setHeldItemSlot(1);
            player.sendMessage("Checa tu inventario");

        }else {
            player.sendMessage("Para clonar un item necesitas estar en el modo Check Point");
        }
    }

    public static String getMapPlayer(final @NotNull Player player){
        return mapPlayer.getOrDefault(player,"");
    }

    public static void goToCheckpoint(final @NotNull Player player, final ItemStack item) {
        final String name_map = mapPlayer.get(player);
        final String checkpoint = DynamicTools.getName(item);
        final CheckpointConfig config = new CheckpointConfig(name_map);
        try {
            config.getCheckpoint(checkpoint);
            final Location location = config.getLocation();
            location.add(0,1,0);
            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());
            TeleportingApi.teleport(player, location);
            SoundApi.playSound(player, 1.0f,  1.0f, "ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT");
        } catch (IOException e) {
            player.sendMessage("Este checkpoint no es Accesible o no Existe");
        }
    }

    public static void loadEditInventoryMap(final @NotNull Player player){
        menu.put(player, checkpoint_Menu_Edit);
        final String name_map = mapPlayer.getOrDefault(player, "");
        DynamicTools.loadCheckpointsItems(name_map);
        player.getInventory().clear();
        player.getInventory().setItem(0, Tools.ONE_CHECKPOINT_MENU.getItem());
        player.getInventory().setItem(1, Tools.LIST_CHECKPOINT_MENU.getItem());
        player.getInventory().setItem(2, Tools.SPAWN_AND_FINISH_MENU.getItem());
        player.getInventory().setItem(3, Tools.CHANGE_ITEM_POSITION.getItem());
        player.getInventory().setItem(6, Tools.REMOVE_MAP.getItem());
        player.getInventory().setItem(7, Tools.OPEN_INVENTORY_ITEM.getItem());
        player.getInventory().setItem(8, Tools.BACK_ITEM.getItem());

        final int v = 27;
        if (StateTools.DISTANCE_BLOCK.getSlot() != v) StateTools.DISTANCE_BLOCK.setSlot(v);
        if (BooleanTools.SET_FLOATING_BLOCKS.getToggle()) {
            StateTools.DISTANCE_BLOCK.setItemSlot(player.getInventory());
        }
        SoundApi.playSound(player, 1.0f, 1.0f, "LEVEL_UP", "ENTITY_PLAYER_LEVELUP");
    }

    public static void changeStates(final @NotNull StateTools stateTools){
        stateTools.nextState(6);
        for (Map.Entry<Player, String> entry : menu.entrySet()){
            final Player player = entry.getKey();
            final String name_inventory = entry.getValue();
            if (name_inventory.equals(checkpoint_Menu_Edit)){
                final PlayerInventory playerInventory = player.getInventory();
                stateTools.setItemSlot(playerInventory);
                SoundApi.playSound(player, 1.0f,  ((float) stateTools.getValue() / 3)+0.3f, "NOTE_BASS", "BLOCK_NOTE_BLOCK_BASS");
            }
        }
    }

    public static void updateToggles(final @NotNull BooleanTools booleanTools) {
        booleanTools.toggle();

        for (Map.Entry<Player, String> entry : menu.entrySet()) {
            final Player player = entry.getKey();
            final String name_inventory = entry.getValue();

            if (name_inventory.equals(checkpoint_Menu_Edit)) {

                booleanTools.setItemSlot(menuOptions);
                if (BooleanTools.SET_FLOATING_BLOCKS.getToggle()){
                    StateTools.DISTANCE_BLOCK.setItemSlot(player.getInventory());
                }else {
                    player.getInventory().setItem(StateTools.DISTANCE_BLOCK.getSlot(), null);

                }
                SoundApi.playSound(player, 1.0f, 2.0f, "CLICK", "UI_BUTTON_CLICK");
            }
        }
    }

    public static void changeItems(final @NotNull Player player) {
        final String name_map = mapPlayer.get(player);
        final List<ItemStack> checkpointItems = DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map);

        if (checkpointItems != null && checkpointItems.size() > 1) {
            final int items = checkpointItems.size() + 2;
            final int slots = ((items + 8) / 9) * 9;
            final Map<Integer, ItemStack> mapItemOrder = new HashMap<>();
            final Inventory reorderInventory = Bukkit.createInventory(null, slots, order);

            for (int i = 0; i < checkpointItems.size(); i++) {
                reorderInventory.setItem(i, checkpointItems.get(i));
                mapItemOrder.put(i, checkpointItems.get(i));
            }

            reorderInventory.setItem(slots - 2, new ItemStack(Tools.APPLY_CHANGES_ITEM.getItem()));
            mapItemOrder.put(slots - 2, Tools.APPLY_CHANGES_ITEM.getItem());
            reorderInventory.setItem(slots - 1, new ItemStack(Tools.CANCEL_CHANGES_ITEM.getItem()));
            mapItemOrder.put(slots - 1, Tools.CANCEL_CHANGES_ITEM.getItem());

            originalInventories.put(player, mapItemOrder);
            player.openInventory(reorderInventory);
            SoundApi.playSound(player, 1.0f, 1.0f, "CLICK", "UI_BUTTON_CLICK");
        } else {
            SoundApi.playSound(player, 1.0f, 0.5f, "VILLAGER_IDLE", "ENTITY_VILLAGER_NO");
            player.sendMessage("Necesitas al menos 2 checkpoints si quieres ordenarlos");
        }
    }

    public static void cancelChangesTop(final @NotNull Player player) {
        if (originalInventories.containsKey(player)) {
            final Map<Integer, ItemStack> inventoryMap = originalInventories.get(player);
            final Inventory playerInventory = player.getOpenInventory().getTopInventory();
            playerInventory.clear();
            inventoryMap.forEach(playerInventory::setItem);
            player.sendMessage("Los cambios han sido cancelados y tu inventario ha sido restaurado.");
        } else {
            player.sendMessage("No se encontró un inventario original para restaurar.");
        }
    }

    public static void applyChangesCheckpoints(final @NotNull Player player) {
        final String name_map = mapPlayer.get(player);
        boolean containsSpaces = false;
        boolean isIdentical = true;
        final Inventory topInventory = player.getOpenInventory().getTopInventory();
        final List<ItemStack> checkpointItems = new ArrayList<>();
        for (int i = 0; i < topInventory.getSize(); i++) {
            if (checkpointItems.size() == DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map).size()) break;
            final ItemStack item = topInventory.getItem(i);
            if (item != null && DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map).contains(item)) checkpointItems.add(item);
            else containsSpaces = true;
        }
        if (checkpointItems.size() == DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map).size()) {
            for (int j = 0; j < checkpointItems.size(); j++) {
                if (!checkpointItems.get(j).isSimilar(DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map).get(j))) {
                    isIdentical = false;
                    break;
                }
            }
            if (isIdentical) {
                player.sendMessage(!containsSpaces ? "Ningun Cambio Realizado" : "Agrupando");
                if (!containsSpaces) return;

                for (int i = 0; i < topInventory.getSize(); i++) {
                    if (DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map).contains(topInventory.getItem(i))) {
                        topInventory.setItem(i, null);
                    }
                }
                DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map).forEach(topInventory::addItem);
                return;
            }

            player.sendMessage("_______________________________________");
            player.sendMessage("Ordenando");
            final Map<Integer, String> checkpointNames = new HashMap<>();
            for (int i = 0; i < checkpointItems.size(); i++) {
                final String checkpointItemName = DynamicTools.getName(checkpointItems.get(i));
                checkpointNames.put(i, checkpointItemName);
            }
            final Map<Integer, ItemStack> mapItemOrder = new HashMap<>();
            for (int i = 0; i < topInventory.getSize(); i++) {
                final ItemStack item = topInventory.getItem(i);
                if (item != null && (item.equals(Tools.APPLY_CHANGES_ITEM.getItem()) || item.equals(Tools.CANCEL_CHANGES_ITEM.getItem()))) {
                    mapItemOrder.put(i, item);
                    continue;
                }
                topInventory.setItem(i, null);
            }
            CheckpointConfig checkpointConfig = new CheckpointConfig(name_map);
            checkpointConfig.ChangePositionsCheckpoint(checkpointNames);
            player.sendMessage("---------------------------------------");
            checkpointNames.forEach((i, name) -> player.sendMessage("Checkpoint " + (i + 1) + " → " + name));
            DynamicTools.loadCheckpointsItems(name_map);
            for (int i = 0; i < DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map).size(); i++) {
                topInventory.setItem(i, DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map).get(i));
                mapItemOrder.put(i, DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map).get(i));
            }
            originalInventories.put(player, mapItemOrder);
            HOLOGRAM_API.reorderArmorStandNames(name_map, Type.CHECKPOINT);
            SoundApi.playSound(player, 1.0f, 2.0f, "BLOCK_ANVIL_USE", "ANVIL_USE");
        } else {
            player.sendMessage("Coloca todos los items que sostienes dentro de los slots.");
            SoundApi.playSound(player,  1.0f, 2.0f, "BLOCK_ANVIL_BREAK", "ANVIL_BREAK");
        }
        player.sendMessage("_______________________________________");
    }

    //---------------------------------------------------------------------------------[PAGES]
    //----------------------------------------------------------------------------------------

    public static void nextPages(final Player player) {
        final String name_map = mapPlayer.get(player);
        final String name_menu = menu.getOrDefault(player, "");

        switch (name_menu) {
            case main_Menu:
                nextPage(player, DynamicTools.SELECTS_MAPS_ITEMS, INDEX_MAPS, ITEMS_PER_PAGE_MAPS);
                break;
            case checkpoint_menu:
                nextPage(player, DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map), INDEX_CHECKPOINT, ITEMS_PER_PAGE_CHECKPOINT);
                break;
            default:
                player.sendMessage("Menú no reconocido: " + name_menu);
                break;
        }
        SoundApi.playSound(player, 1.0f, 1.0f, "CLICK", "UI_BUTTON_CLICK");
    }

    public static void previousPages(final Player player) {
        final String name_map = mapPlayer.get(player);
        final String name_menu = menu.getOrDefault(player, "");

        switch (name_menu) {
            case main_Menu:
                previousPage(player, DynamicTools.SELECTS_MAPS_ITEMS, INDEX_MAPS, ITEMS_PER_PAGE_MAPS);
                break;
            case checkpoint_menu:
                previousPage(player, DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map), INDEX_CHECKPOINT, ITEMS_PER_PAGE_CHECKPOINT);
                break;
            default:
                player.sendMessage("Menú no reconocido: " + name_menu);
                break;
        }
        SoundApi.playSound(player, 1.0f, 1.0f, "CLICK", "UI_BUTTON_CLICK");
    }

    private static void showPage(final @NotNull Player player, final int page, @NotNull final List<ItemStack> items, final int Index, final int items_for_page) {
        for (int i = Index; i < Index + items_for_page; i++) {
            player.getInventory().setItem(i, null);
        }
        int slotIndex = Index;
        int startIndex = page * items_for_page;
        int endIndex = Math.min(startIndex + items_for_page, items.size());
        for (int i = startIndex; i < endIndex; i++) {
            player.getInventory().setItem(slotIndex, items.get(i));
            slotIndex++;
        }
        if (page > 0) player.getInventory().setItem(Index-1, Tools.PREVIOUS_PAGE_ITEM.getItem());
        else player.getInventory().setItem(Index-1, null);
        if (endIndex < items.size()) player.getInventory().setItem(Index+items_for_page, Tools.NEXT_PAGE_ITEM.getItem());
        else player.getInventory().setItem(Index+items_for_page, null);
    }

    private static void nextPage(final Player player, final @NotNull List<ItemStack> itemsSize, final int slotIndex ,final int items_for_page) {
        int currentPage = playerPages.getOrDefault(player, 0);
        final int maxPage = (int) Math.ceil((double) itemsSize.size() / items_for_page) - 1;
        if (currentPage < maxPage) {
            currentPage++;
            playerPages.put(player, currentPage);
            showPage(player, currentPage, itemsSize, slotIndex, items_for_page);
        }
    }

    private static void previousPage(final Player player, final @NotNull List<ItemStack> itemsSize, final int slotIndex ,final int items_for_page) {
        int currentPage = playerPages.getOrDefault(player, 0);
        if (currentPage > 0) {
            currentPage--;
            playerPages.put(player, currentPage);
            showPage(player, currentPage, itemsSize, slotIndex, items_for_page);
        }
    }
    //----------------------------------------------------------------------------------[UPDATES]
    //----------------------------------------------------------------------------------------

    public static void updateInventory(final Player player) {
        final String menu_player = menu.getOrDefault(player, "");

        final String map_name = mapPlayer.get(player);

        switch (menu_player) {
            case main_Menu:
                showPage(player, playerPages.getOrDefault(player, 0), DynamicTools.SELECTS_MAPS_ITEMS, INDEX_MAPS, ITEMS_PER_PAGE_MAPS);
                break;
            case checkpoint_menu:
                if (map_name != null) {
                    showPage(player, playerPages.getOrDefault(player, 0), DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(map_name), INDEX_CHECKPOINT, ITEMS_PER_PAGE_CHECKPOINT);
                }
                break;
            case checkpoint_Menu_Edit:
                final String inventory = player.getOpenInventory().getTitle();
                if (inventory.equals(order)) {
                    player.closeInventory();
                }
                break;
            case spawnAndFinishMenu:
                if (map_name != null) {
                    final List<ItemStack> all = new ArrayList<>(DynamicTools.SPAWN_LOCATIONS.get(map_name));
                    all.addAll(DynamicTools.FINISH_LOCATION.get(map_name));
                    showPage(player, playerPages.getOrDefault(player, 0), all, INDEX_CHECKPOINT, ITEMS_PER_PAGE_CHECKPOINT);
                }
                break;
            default:
                player.sendMessage("Valor inesperado para menu_player: " + menu_player);
                break;
        }
    }

    public static void backInventory(final Player player) {
        playerPages.put(player, 0);
        final String name_map = mapPlayer.get(player);
        final String menu_player = menu.getOrDefault(player, "");

        switch (menu_player) {
            case checkpoint_Menu_Edit:
                if (name_map != null) HOLOGRAM_API.hideHolograms(player, name_map, Type.CHECKPOINT);
                mapPlayer.remove(player);
                loadMainInventory(player);
                break;
            case checkpoint_menu:
                if (name_map != null) loadEditInventoryMap(player);
                break;
            case spawnAndFinishMenu:
                loadEditInventoryMap(player);
                break;
            default:
                player.sendMessage("Valor inesperado para menu_player: " + menu_player);
                break;
        }
        //SoundApi.playSound(player, 1.0f,  1.0f, "SHEEP_SHEAR", "BLOCK_GLASS_PLACE");
        SoundApi.playSound(player, 1.0f, 1.0f, "CLICK", "UI_BUTTON_CLICK");
    }

    //---------------------------------------------------------------------------------[BLOCK]
    //----------------------------------------------------------------------------------------

    public static boolean CreatedMap(final Player player, final @NotNull String map){
        final List<String> RESERVED_NAMES = Collections.unmodifiableList(Arrays.asList(
                "com", "com1", "aux", "nul", "prn", "con",
                "lpt1", "lpt2", "lpt3", "clock$", "config$", "desktop$"
        ));
        if (map.isEmpty()) {
            player.sendMessage("Debes ingresar un Nombre de tu mapa en la primera Linea.");
            return false;
        }
        if (RESERVED_NAMES.contains(map.toLowerCase())) {
            player.sendMessage("Error: El nombre del mapa '" + map + "' es reservado y no se puede usar.");
            return false;
        }
        if (!map.matches("^[a-zA-Z0-9_\\- ()]{3,50}$")) {
            player.sendMessage("Error: El nombre del mapa '" + map + "' contiene caracteres no permitidos o es demasiado corto/largo.");
            return false;
        }
        final String name_map = Configuration.getUniqueFolderName(map);
        Configuration.createMapFolder(name_map);
        DynamicTools.SELECTS_MAPS_ITEMS.add(DynamicTools.createItemMap(name_map));
        refreshAllMaps();
        SoundApi.playSound(player, 1.0f, 1.0f, "BLOCK_ANVIL_USE", "ANVIL_USE");
        return true;
    }

    public static void updateSignMap(final Player player, final String mapName, final Sign sign){
        if (mapName != null) {
            if (CreatedMap(player, mapName)) {
                final Location location = sign.getLocation();
                Kit.getRegionScheduler().runDelayed(plugin, location, t ->
                        Kit.getRegionScheduler().execute(plugin, location, ()->{
                            if (!tempBlock.containsKey(player)) return;
                            sign.setLine(0, "Creado");
                            sign.setLine(1, "Exitosamente");
                            sign.setLine(2, "Nombre");
                            sign.setLine(3, mapName);
                            sign.update();
                        }), 20L);
                Kit.getRegionScheduler().runDelayed(plugin, location, t -> destroyBlock(player), 40L);
            }else {
                destroyBlock(player);
            }
        }
    }

    public static void destroyBlock(final Player player) {
        if (!plugin.isEnabled()) return;
        final Block temp = tempBlock.get(player);
        if (temp != null && temp.getType() != Material.AIR) {
            final Location location = temp.getLocation();
            Kit.getRegionScheduler().execute(plugin, location, () -> {
                temp.setType(Material.AIR);
                location.getWorld().createExplosion(location, 0);
                tempBlock.remove(player);
                BlockCache.deleteByIdOneBlockCache(player.getUniqueId());
            });
        }
    }

    //----------------------------------------------------------------------------[VALIDATORS]
    //----------------------------------------------------------------------------------------

    public static boolean isEntityArmorStandOfGUI(final UUID uuid){
        for (final UUID uuidArmors : EntityCache.getEntityCache().get(EntityType.ARMOR_STAND)){
            if (uuidArmors.equals(uuid)){
                return true;
            }
        }
        return false;
    }

    //----------------------------------------------------------------------------------[EXIT]
    //----------------------------------------------------------------------------------------

    public static void exitEditMode(final Player player) {
        if (editingPlayers.containsKey(player)) {
            final UUID uuid = player.getUniqueId();
            destroyBlock(player);
            final ItemStack[] savedInventory = playerInventories.get(player);
            if (savedInventory != null) player.getInventory().setContents(savedInventory);
            playerInventories.remove(player);
            editingPlayers.remove(player);
            originalInventories.remove(player);
            final String name_map = mapPlayer.get(player);
            if (name_map != null) HOLOGRAM_API.hideHolograms(player, name_map, Type.CHECKPOINT);
            mapPlayer.remove(player);
            menu.remove(player);
            DynamicTools.getUniquePlayerItem().remove(player);
            InventoryCache.removeInventory(uuid);
            SoundApi.playSound(player, 1.0f, 1.0f, "NOTE_BASS", "BLOCK_NOTE_BLOCK_BASS");
        }
        if (editingPlayers.isEmpty() && isActiveListener){
            HandlerList.unregisterAll(GUI_LISTENER);
            if (ApiCompatibility.HAS_OFF_HAND_METHOD()) HandlerList.unregisterAll(MODERN_GUI_LISTENER);
            isActiveListener = false;
        }
    }

    public static boolean isInEditMode(final Player player) {
        return editingPlayers.getOrDefault(player, false);
    }
}