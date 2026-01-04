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
import org.astral.parkour_plugin.gui.editor.postSign.TextSignApi;
import org.astral.parkour_plugin.views.tag_name.ArmorStandApi;
import org.astral.parkour_plugin.views.Type;
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

    private static final Map<UUID, PlayerDataGui> DatGui = new HashMap<>();

    
    public static @NotNull Set<UUID> playersInEdit(){
        return new HashSet<>(DatGui.keySet());
    }

    //------------------------------------------------------------------------------[EDITOR SETTINGS]
    //-----------------------------------------------------------------------------------------------

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
    private static final byte ITEMS_PER_PAGE_SPAWN_FINISH = 3;


    public static final String order = "Ordena Tus Checkpoints";
    public static final Map<Player, Block> tempBlock = new HashMap<>();

    private static final String main_Menu = "Main_Menu";
    private static final String menuOfMap = "Menu_Of_Map";
    private static final String checkpoint_menu = "Checkpoint_Menu";
    private static final String spawnAndFinishMenu = "Spawn_End_Menu";

    private static final Inventory menuOptions = Bukkit.createInventory(null, 9, "Opciones");

    private static final ArmorStandApi HOLOGRAM_API = plugin.getArmorStandApi();
    private static final TextSignApi TEXT_SIGN_API = TextSignApi._text(plugin);

    private static final GuiListener GUI_LISTENER = new GuiListener();
    private static final ModernGuiListener MODERN_GUI_LISTENER = new ModernGuiListener();
    private static boolean isActiveListener = false;

    //----------------------------------------------------------------------------[Lobby's]
    //-------------------------------------------------------------------------------------
    public static final String lobbyMenuSelectorGlobal = "Selector de Lobby's Globales";
    // LOBBY'S
    private static final byte INDEX_LOBBY = 0;
    private static final byte ITEMS_PER_PAGE_LOBBY = 52;


    //-----------------------------------------------------------------------[ITEMS  LOBBY]
    //-------------------------------------------------------------------------------------
    public static final String itemsInventoryMenu = "Selecciona el Mapa";
    public static final String orderInventoryItems = "Ordenar Items";

    //----------------------------------------------------------------------------[Events]
    //------------------------------------------------------------------------------------
    private static void registerOrUnregisterEvents(){
        if (!DatGui.isEmpty() && !isActiveListener) {
            plugin.getServer().getPluginManager().registerEvents(GUI_LISTENER, plugin);
            if (ApiCompatibility.HAS_OFF_HAND_METHOD()) {
                plugin.getServer().getPluginManager().registerEvents(MODERN_GUI_LISTENER, plugin);
            }
            isActiveListener = true;
        }
    }

    //---------------------------------------------------------------------------------[ITEMS]
    //----------------------------------------------------------------------------------------

    public static void loadInventoryOfItems(@NotNull Player player){
        final UUID uuid = player.getUniqueId();
        exitGui(Bukkit.getPlayer(uuid));
        PlayerDataGui data = DatGui.computeIfAbsent(uuid, k -> new PlayerDataGui());
        registerOrUnregisterEvents();
        Inventory inventory = Bukkit.createInventory(player, 54, itemsInventoryMenu);
        List<ItemStack> items = new ArrayList<>(DynamicTools.SELECTS_MAPS_ITEMS);
        int initialPage = 0;
        data.setPage(initialPage);
        data.setMenu(itemsInventoryMenu);
        showPage(inventory, initialPage, items);
        player.openInventory(inventory);

    }

    public static void loadInventoryItemsEdit(@NotNull Player player, String map){
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.computeIfAbsent(uuid, k -> new PlayerDataGui());
        data.setMenu(orderInventoryItems);
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        topInventory.clear();
        //topInventory.setItem();
        SoundApi.playSound(player, 1.0f, 1.0f, "CLICK", "UI_BUTTON_CLICK");
    }

    //----------------------------------------------------------------------[SELECTOR LOBBY'S]
    //----------------------------------------------------------------------------------------

    public static void loadInventorySelectorGlobal(@NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        exitGui(Bukkit.getPlayer(uuid));
        PlayerDataGui data = DatGui.computeIfAbsent(uuid, k -> new PlayerDataGui());
        registerOrUnregisterEvents();
        DynamicTools.refreshLobbyItems();
        Inventory personalInventory = Bukkit.createInventory(player, 54, lobbyMenuSelectorGlobal);
        List<ItemStack> items = new ArrayList<>(DynamicTools.LOBBY_STATE.values());
        int initialPage = 0;
        data.setPage(initialPage);
        showPage(personalInventory, initialPage, items);
        data.setMenu(lobbyMenuSelectorGlobal);
        player.openInventory(personalInventory);
    }

    public static void closeInventoryGlobal(final @NotNull Player player){
        final UUID uuid = player.getUniqueId();
        DatGui.remove(uuid);
        registerOrUnregisterEvents();
    }

    public static String getMenu(final @NotNull Player player){
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.computeIfAbsent(uuid, k -> new PlayerDataGui());
        return data.getMenu();
    }

    private static void showPage(@NotNull Inventory inventory, final int page, @NotNull final List<ItemStack> items) {
        for (int i = Gui.INDEX_LOBBY; i < (int) Gui.INDEX_LOBBY + (int) Gui.ITEMS_PER_PAGE_LOBBY; i++) {
            inventory.setItem(i, null);
        }
        int slotIndex = Gui.INDEX_LOBBY;
        int startIndex = page * (int) Gui.ITEMS_PER_PAGE_LOBBY;
        int endIndex = Math.min(startIndex + (int) Gui.ITEMS_PER_PAGE_LOBBY, items.size());
        for (int i = startIndex; i < endIndex; i++) {
            inventory.setItem(slotIndex, items.get(i));
            slotIndex++;
        }
        int previousSlot = 52;
        int nextSlot = 53;
        if (page > 0) inventory.setItem(previousSlot, Tools.PREVIOUS_PAGE_ITEM.getItem());
        else inventory.setItem(previousSlot, null);

        if (endIndex < items.size()) inventory.setItem(nextSlot, Tools.NEXT_PAGE_ITEM.getItem());
        else inventory.setItem(nextSlot, null);
    }

    private static void nextInventoryPage(@NotNull Player player, @NotNull List<ItemStack> items) {
        UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        int currentPage = data.getPage();
        int maxPage = (int) Math.ceil((double) items.size() / (int) Gui.ITEMS_PER_PAGE_LOBBY) - 1;

        if (currentPage < maxPage) {
            currentPage++;
            data.setPage(currentPage);

            Inventory inventory = player.getOpenInventory().getTopInventory();
            showPage(inventory, currentPage, items);
        }
    }

    private static void previousInventoryPage(@NotNull Player player, @NotNull List<ItemStack> items) {
        UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        int currentPage = data.getPage();
        if (currentPage > 0) {
            currentPage--;
            data.setPage(currentPage);

            Inventory inventory = player.getOpenInventory().getTopInventory();
            showPage(inventory, currentPage, items);
        }
    }

    public static void updateItemInLobbyInventories(String mapName) {
        if (DatGui.isEmpty())return;
        ItemStack updatedItem = DynamicTools.createItemLobbyGlobal(mapName);
        for (Map.Entry<UUID, PlayerDataGui> entry : DatGui.entrySet()) {
            UUID uuid = entry.getKey();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            PlayerDataGui data = entry.getValue();
            if (!lobbyMenuSelectorGlobal.equals(data.getMenu())) continue;

            int currentPage = data.getPage();
            Inventory inventory = player.getOpenInventory().getTopInventory();
            List<ItemStack> allItems = new ArrayList<>(DynamicTools.LOBBY_STATE.values());

            int start = currentPage * ITEMS_PER_PAGE_LOBBY;
            int end = Math.min(start + ITEMS_PER_PAGE_LOBBY, allItems.size());

            for (int i = start, slot = INDEX_LOBBY; i < end; i++, slot++) {
                ItemStack item = allItems.get(i);
                String itemName = DynamicTools.getName(item);
                if (itemName != null && itemName.equalsIgnoreCase(mapName)) {
                    inventory.setItem(slot, updatedItem);
                    break;
                }
            }
        }
    }

    //--------------------------------------------------------------------------------[EDITOR]
    //----------------------------------------------------------------------------------------
    public static void enterEditMode(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.computeIfAbsent(uuid, k -> new PlayerDataGui());

        if (!data.isEditing()) {
            final ItemStack[] itemStacks = player.getInventory().getContents();
            data.setPlayerInventories(itemStacks);
            InventoryCache.saveInventory(uuid, itemStacks);
            loadMainInventoryEdit(player);
            data.setEditing(true);
            SoundApi.playSound(player, 1.0f, 2.0f, "ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP");
        }
        registerOrUnregisterEvents();
    }

    public static void loadMainInventoryEdit(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.computeIfAbsent(uuid, k -> new PlayerDataGui());

        data.setMenu(main_Menu);
        data.setPage(0);

        player.getInventory().clear();
        player.getInventory().setItem(0, Tools.ADD_MAP_ITEM.getItem());
        player.getInventory().setItem(8, Tools.EXIT_ITEM.getItem());

        showPage(player, 0, DynamicTools.SELECTS_MAPS_ITEMS, INDEX_MAPS, ITEMS_PER_PAGE_MAPS);
    }

    //----------------------------------------------------------------------[SPAWN AND FINISH]
    //----------------------------------------------------------------------------------------
    public static void loadSpawnAndEndMenu(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.computeIfAbsent(uuid, k -> new PlayerDataGui());

        data.setMenu(spawnAndFinishMenu);
        data.setPage(0);

        final String name_map = data.getMapPlayer();

        DynamicTools.loadSpawnPoints(name_map);
        DynamicTools.loadFinishPoints(name_map);

        player.getInventory().clear();
        player.getInventory().setItem(0, Tools.MARK_SPAWN_ITEM.getItem());
        player.getInventory().setItem(1, Tools.MARK_FINISH_ITEM.getItem());
        player.getInventory().setItem(8, Tools.BACK_ITEM.getItem());

        final List<ItemStack> all = new ArrayList<>(DynamicTools.SPAWN_LOCATIONS.get(name_map));
        all.addAll(DynamicTools.FINISH_LOCATION.get(name_map));

        showPage(player, 0, all, INDEX_SPAWN_FINISH, ITEMS_PER_PAGE_SPAWN_FINISH);
    }

    //----------------------------------------------------------------------------[CHECKPOINT]
    //----------------------------------------------------------------------------------------
    public static void loadOneCheckpointMap(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.computeIfAbsent(uuid, k -> new PlayerDataGui());

        data.setMenu(checkpoint_menu);
        data.setPage(0);

        final String name_map = data.getMapPlayer();

        player.getInventory().clear();

        showPage(player, 0, DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map), INDEX_CHECKPOINT, ITEMS_PER_PAGE_CHECKPOINT);

        player.getInventory().setItem(0, Tools.CHECKPOINT_MARKER.getItem());
        player.getInventory().setItem(1, DynamicTools.getUniquePlayerItem().getOrDefault(player, null));
        player.getInventory().setItem(7, Tools.EDIT_FEATHER_ITEM.getItem());
        player.getInventory().setItem(8, Tools.BACK_ITEM.getItem());
        player.getInventory().setItem(35, Tools.REMOVE_MAP.getItem());


        SoundApi.playSound(player, 1.0f, 1.0f, "CLICK", "UI_BUTTON_CLICK");
    }

    //-------------------------------------------------------------[REPLACE-EDITOR-DELETE-ADD]
    //----------------------------------------------------------------------------------------

    public static void addMap(final @NotNull Player player){
        TEXT_SIGN_API.AddNewMap(player);
    }

    public static void removeMap(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String name_map = data.getMapPlayer();
        if (name_map.isEmpty()) return;

        Configuration.deleteMapFolder(name_map);
        removeMaps(name_map);
    }

    public static void addSpawnPoint(final @NotNull Player player, final Location location) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String name_map = data.getMapPlayer();
        Rules rules = new Rules(name_map);

        if (rules.isEqualsLocation(location)) {
            player.sendMessage("Esta ubicación ya se marcó como spawn");
        } else {
            HOLOGRAM_API.addHologram(name_map, rules.setSpawns(location), location, Type.SPAWN);
            updateAllSpawnEnd(name_map);
        }
    }

    public static void addEndPoint(final @NotNull Player player, final Location location) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String name_map = data.getMapPlayer();
        Rules rules = new Rules(name_map);

        if (rules.isEqualsLocation(location)) {
            player.sendMessage("Esta ubicación ya se marcó como punto final");
        } else {
            HOLOGRAM_API.addHologram(name_map, rules.setEndPoints(location), location, Type.END_POINT);
            updateAllSpawnEnd(name_map);
        }
    }

    public static void removeSpawnPoint(final @NotNull Player player, final Location location) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String name_map = data.getMapPlayer();
        Rules rules = new Rules(name_map);

        if (rules.isEqualsLocation(location)) {
            HOLOGRAM_API.removeHologram(name_map, rules.getSpawnKeyFromLocation(location), Type.SPAWN);
            rules.removeSpawnPoint(location);
            updateAllSpawnEnd(name_map);
            HOLOGRAM_API.reorderArmorStandNames(name_map, Type.SPAWN);
        }
    }

    public static void removeEndPoint(final @NotNull Player player, final Location location) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String name_map = data.getMapPlayer();
        Rules rules = new Rules(name_map);

        if (rules.isEqualsLocation(location)) {
            HOLOGRAM_API.removeHologram(name_map, rules.getEndPointKeyFromLocation(location), Type.END_POINT);
            rules.removeEndPoint(location);
            updateAllSpawnEnd(name_map);
            HOLOGRAM_API.reorderArmorStandNames(name_map, Type.END_POINT);
        }
    }

    public static void addCheckpoint(final @NotNull Player player, final Location location) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String name_map = data.getMapPlayer();
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
        checkpointConfig.setMaxFallY(CheckpointConfig.MAX_Y);
        HOLOGRAM_API.addHologram(name_map, checkpoint, checkpointConfig.getLocation(), Type.CHECKPOINT);
        updateCheckpoints(name_map);
    }

    public static void removeCheckpoint(final @NotNull Player player, final Location location){
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String name_map = data.getMapPlayer();
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

    public static void reorderCheckpoints(final @NotNull Player player){
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String name_map = data.getMapPlayer();
        CheckpointConfig checkpointConfig = new CheckpointConfig(name_map);
        checkpointConfig.reorderCheckpoints();
        HOLOGRAM_API.reorderArmorStandNames(name_map, Type.CHECKPOINT);
        updateCheckpoints(name_map);
    }

    public static void refreshAllMaps() {
        Kit.getAsyncScheduler().runNow(plugin, t -> {
            DynamicTools.refreshMaps();
            DynamicTools.refreshLobbyItems(); // Refresca también los ítems del lobby global

            for (final Map.Entry<UUID, PlayerDataGui> entry : DatGui.entrySet()) {
                final UUID uuid = entry.getKey();
                final PlayerDataGui data = entry.getValue();
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;

                final String menu = data.getMenu();

                if (main_Menu.equals(menu) || lobbyMenuSelectorGlobal.equals(menu)) {
                    updateInventory(player);
                }
            }
        });
    }

    private static void removeMaps(final String name_map) {
        Kit.getAsyncScheduler().runNow(plugin, t -> {
            DynamicTools.refreshMaps();
            DynamicTools.refreshLobbyItems();
            for (final Map.Entry<UUID, PlayerDataGui> entry : DatGui.entrySet()) {
                final UUID uuid = entry.getKey();
                final PlayerDataGui data = entry.getValue();
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                final String name = data.getMapPlayer();
                final String nameMenu = data.getMenu();
                if (nameMenu.equals(lobbyMenuSelectorGlobal)) {
                    updateInventory(player);
                }
                if (name.equals(name_map)) {
                    final String inventory = player.getOpenInventory().getTitle();
                    if (inventory.equals(order)) player.closeInventory();
                    for (Type type : Type.values()) {
                        HOLOGRAM_API.hideHolograms(player, name_map, type);
                    }
                    loadMainInventoryEdit(player);
                    SoundApi.playSound(player, 1.0f, 1.0f, "ITEM_BREAK", "ENTITY_ITEM_BREAK");
                }

            }
            DynamicTools.CHECKPOINTS_MAPS_ITEMS.remove(name_map);
        });
    }

    private static void updateCheckpoints(final String name_map) {
        Kit.getAsyncScheduler().runNow(plugin, t -> {
            DynamicTools.loadCheckpointsItems(name_map);

            for (Map.Entry<UUID, PlayerDataGui> entry : DatGui.entrySet()) {
                final UUID uuid = entry.getKey();
                final PlayerDataGui data = entry.getValue();
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;

                final String nameMap = data.getMapPlayer();

                if (name_map.equals(nameMap)) {
                    updateInventory(player);
                }
            }
        });
    }

    private static void updateAllSpawnEnd(final String name_map) {
        Kit.getAsyncScheduler().runNow(plugin, t -> {
            DynamicTools.loadSpawnPoints(name_map);
            DynamicTools.loadFinishPoints(name_map);

            for (Map.Entry<UUID, PlayerDataGui> entry : DatGui.entrySet()) {
                final UUID uuid = entry.getKey();
                final PlayerDataGui data = entry.getValue();
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;

                final String nameMap = data.getMapPlayer();
                if (name_map.equals(nameMap)) {
                    updateInventory(player);
                }
            }
        });
    }

    //------------------------------------------------------------------------------------[Gui]
    //----------------------------------------------------------------------------------------
    public static void setMapPlayer(final @NotNull Player player, final String name_map) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.computeIfAbsent(uuid, k -> new PlayerDataGui());
        data.setMapPlayer(name_map);
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

    public static void setItemModifiable(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);

        if (data != null && checkpoint_menu.equals(data.getMenu())) {
            player.getInventory().setItem(1, DynamicTools.getUniquePlayerItem().getOrDefault(player, null));
            player.getInventory().setHeldItemSlot(1);
            player.sendMessage("Checa tu inventario");
        } else {
            player.sendMessage("Para clonar un item necesitas estar en el modo Check Point");
        }
    }

    public static String getMapPlayer(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        return data != null ? data.getMapPlayer() : "";
    }

    public static void goToCheckpoint(final @NotNull Player player, final ItemStack item) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;
        final String name_map = data.getMapPlayer();
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

    public static void gotoSpawnPoint(final @NotNull Player player, final ItemStack item){
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;
        final String name_map = data.getMapPlayer();
        final String key = DynamicTools.getName(item);
        final Rules rules = new Rules(name_map);
        final Location location = rules.getSpawnLocationFromKey(key);
        if (location == null) return;
        location.add(0,1,0);
        location.setPitch(player.getLocation().getPitch());
        location.setYaw(player.getLocation().getYaw());
        TeleportingApi.teleport(player, location);
    }

    public static void gotoEndPoint(final @NotNull Player player, final ItemStack item) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;
        final String name_map = data.getMapPlayer();
        final String key = DynamicTools.getName(item);
        final Rules rules = new Rules(name_map);
        final Location location = rules.getEndPointLocationFromKey(key);
        if (location == null) return;
        location.add(0,1,0);
        location.setPitch(player.getLocation().getPitch());
        location.setYaw(player.getLocation().getYaw());
        TeleportingApi.teleport(player, location);
    }

    public static void loadEditInventoryMap(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.computeIfAbsent(uuid, k -> new PlayerDataGui());

        data.setMenu(menuOfMap);
        final String name_map = data.getMapPlayer();

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
        for (Type type : Type.values()) {
            HOLOGRAM_API.showHolograms(player, name_map, type);
        }
        SoundApi.playSound(player, 1.0f, 1.0f, "LEVEL_UP", "ENTITY_PLAYER_LEVELUP");
    }

    public static void changeStates(final @NotNull StateTools stateTools) {
        stateTools.nextState(6);

        for (Map.Entry<UUID, PlayerDataGui> entry : DatGui.entrySet()) {
            final UUID uuid = entry.getKey();
            final PlayerDataGui data = entry.getValue();

            if (menuOfMap.equals(data.getMenu())) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;

                final PlayerInventory playerInventory = player.getInventory();
                stateTools.setItemSlot(playerInventory);

                SoundApi.playSound(player, 1.0f, ((float) stateTools.getValue() / 3) + 0.3f, "NOTE_BASS", "BLOCK_NOTE_BLOCK_BASS");
            }
        }
    }

    public static void updateToggles(final @NotNull BooleanTools booleanTools) {
        booleanTools.toggle();
        for (Map.Entry<UUID, PlayerDataGui> entry : DatGui.entrySet()) {
            final UUID uuid = entry.getKey();
            final PlayerDataGui data = entry.getValue();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            final String name_inventory = data.getMenu();
            if (menuOfMap.equals(name_inventory)) {
                booleanTools.setItemSlot(menuOptions);
                if (BooleanTools.SET_FLOATING_BLOCKS.getToggle()) {
                    StateTools.DISTANCE_BLOCK.setItemSlot(player.getInventory());
                } else {
                    player.getInventory().setItem(StateTools.DISTANCE_BLOCK.getSlot(), null);
                }
                SoundApi.playSound(player, 1.0f, 2.0f, "CLICK", "UI_BUTTON_CLICK");
            }
        }
    }

    public static void changeItems(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String name_map = data.getMapPlayer();
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

            data.setOriginalInventory(mapItemOrder);

            player.openInventory(reorderInventory);
            SoundApi.playSound(player, 1.0f, 1.0f, "CLICK", "UI_BUTTON_CLICK");
        } else {
            SoundApi.playSound(player, 1.0f, 0.5f, "VILLAGER_IDLE", "ENTITY_VILLAGER_NO");
            player.sendMessage("Necesitas al menos 2 checkpoints si quieres ordenarlos");
        }
    }

    public static void cancelChangesTop(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) {
            player.sendMessage("No se encontró un inventario original para restaurar.");
            return;
        }
        final Map<Integer, ItemStack> inventoryMap = data.getOriginalInventory();
        if (inventoryMap.isEmpty()) {
            player.sendMessage("No se encontró un inventario original para restaurar.");
            return;
        }
        final Inventory playerInventory = player.getOpenInventory().getTopInventory();
        playerInventory.clear();
        inventoryMap.forEach(playerInventory::setItem);
        player.sendMessage("Los cambios han sido cancelados y tu inventario ha sido restaurado.");
    }

    public static void applyChangesCheckpoints(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String name_map = data.getMapPlayer();
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

            data.setOriginalInventory(mapItemOrder);

            HOLOGRAM_API.reorderArmorStandNames(name_map, Type.CHECKPOINT);
            SoundApi.playSound(player, 1.0f, 2.0f, "BLOCK_ANVIL_USE", "ANVIL_USE");
        } else {
            player.sendMessage("Coloca todos los items que sostienes dentro de los slots.");
            SoundApi.playSound(player, 1.0f, 2.0f, "BLOCK_ANVIL_BREAK", "ANVIL_BREAK");
        }
        player.sendMessage("_______________________________________");
    }

    //---------------------------------------------------------------------------------[PAGES]
    //----------------------------------------------------------------------------------------

    public static void nextPages(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) {
            player.sendMessage("Error: Datos del jugador no encontrados.");
            return;
        }
        final String name_map = data.getMapPlayer();
        final String name_menu = data.getMenu();
        switch (name_menu) {
            case main_Menu:
                nextPage(player, DynamicTools.SELECTS_MAPS_ITEMS, INDEX_MAPS, ITEMS_PER_PAGE_MAPS);
                break;
            case checkpoint_menu:
                nextPage(player, DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map), INDEX_CHECKPOINT, ITEMS_PER_PAGE_CHECKPOINT);
                break;
            case spawnAndFinishMenu:
                final List<ItemStack> all = new ArrayList<>(DynamicTools.SPAWN_LOCATIONS.getOrDefault(name_map, Collections.emptyList()));
                all.addAll(DynamicTools.FINISH_LOCATION.getOrDefault(name_map, Collections.emptyList()));
                nextPage(player, all, INDEX_SPAWN_FINISH, ITEMS_PER_PAGE_SPAWN_FINISH);
                break;
            case lobbyMenuSelectorGlobal:
                nextInventoryPage(player, new ArrayList<>(DynamicTools.LOBBY_STATE.values()));
                break;
            default:
                player.sendMessage("Menú no reconocido: " + name_menu);
                break;
        }
        SoundApi.playSound(player, 1.0f, 1.0f, "CLICK", "UI_BUTTON_CLICK");
    }

    public static void previousPages(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) {
            player.sendMessage("Error: Datos del jugador no encontrados.");
            return;
        }

        final String name_map = data.getMapPlayer();
        final String name_menu = data.getMenu();

        switch (name_menu) {
            case main_Menu:
                previousPage(player, DynamicTools.SELECTS_MAPS_ITEMS, INDEX_MAPS, ITEMS_PER_PAGE_MAPS);
                break;
            case checkpoint_menu:
                previousPage(player, DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(name_map), INDEX_CHECKPOINT, ITEMS_PER_PAGE_CHECKPOINT);
                break;
            case spawnAndFinishMenu:
                final List<ItemStack> all = new ArrayList<>(DynamicTools.SPAWN_LOCATIONS.getOrDefault(name_map, Collections.emptyList()));
                all.addAll(DynamicTools.FINISH_LOCATION.getOrDefault(name_map, Collections.emptyList()));
                previousPage(player, all, INDEX_SPAWN_FINISH, ITEMS_PER_PAGE_SPAWN_FINISH);
                break;
            case lobbyMenuSelectorGlobal:
                previousInventoryPage(player, new ArrayList<>(DynamicTools.LOBBY_STATE.values()));
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

    private static void nextPage(final @NotNull Player player, final @NotNull List<ItemStack> itemsSize, final int slotIndex, final int items_for_page) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        int currentPage = data.getPage();
        final int maxPage = (int) Math.ceil((double) itemsSize.size() / items_for_page) - 1;
        if (currentPage < maxPage) {
            currentPage++;
            data.setPage(currentPage);
            showPage(player, currentPage, itemsSize, slotIndex, items_for_page);
        }
    }

    private static void previousPage(final @NotNull Player player, final @NotNull List<ItemStack> itemsSize, final int slotIndex, final int items_for_page) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        int currentPage = data.getPage();
        if (currentPage > 0) {
            currentPage--;
            data.setPage(currentPage);
            showPage(player, currentPage, itemsSize, slotIndex, items_for_page);
        }
    }
    //----------------------------------------------------------------------------------[UPDATES]
    //----------------------------------------------------------------------------------------

    public static void updateInventory(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        final String menu_player = data.getMenu();
        final String map_name = data.getMapPlayer();
        final int currentPage = data.getPage();

        switch (menu_player) {
            case main_Menu:
                showPage(player, currentPage, DynamicTools.SELECTS_MAPS_ITEMS, INDEX_MAPS, ITEMS_PER_PAGE_MAPS);
                break;
            case checkpoint_menu:
                if (map_name != null && !map_name.isEmpty()) {
                    showPage(player, currentPage, DynamicTools.CHECKPOINTS_MAPS_ITEMS.get(map_name), INDEX_CHECKPOINT, ITEMS_PER_PAGE_CHECKPOINT);
                }
                break;
            case menuOfMap:
                final String inventory = player.getOpenInventory().getTitle();
                if (inventory.equals(order)) {
                    player.closeInventory();
                }
                break;
            case spawnAndFinishMenu:
                if (map_name != null && !map_name.isEmpty()) {
                    final List<ItemStack> all = new ArrayList<>(DynamicTools.SPAWN_LOCATIONS.getOrDefault(map_name, Collections.emptyList()));
                    all.addAll(DynamicTools.FINISH_LOCATION.getOrDefault(map_name, Collections.emptyList()));
                    showPage(player, currentPage, all, INDEX_SPAWN_FINISH, ITEMS_PER_PAGE_SPAWN_FINISH);
                }
                break;
            case lobbyMenuSelectorGlobal:
                showPage(player.getOpenInventory().getTopInventory(), currentPage, new ArrayList<>(DynamicTools.LOBBY_STATE.values()));
                break;
            default:
                player.sendMessage("Valor inesperado para menu_player: " + menu_player);
                break;
        }
    }

    public static void backInventory(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        if (data == null) return;

        data.setPage(0);
        final String name_map = data.getMapPlayer();
        final String menu_player = data.getMenu();

        switch (menu_player) {
            case menuOfMap:
                loadMainInventoryEdit(player);
                data.setMapPlayer(null);
                if (name_map != null && !name_map.isEmpty()) {
                    HOLOGRAM_API.hideHolograms(player, name_map, Type.CHECKPOINT);
                    HOLOGRAM_API.hideHolograms(player, name_map, Type.SPAWN);
                    HOLOGRAM_API.hideHolograms(player, name_map, Type.END_POINT);
                }
                break;
            case checkpoint_menu:
            case spawnAndFinishMenu:
                loadEditInventoryMap(player);
                break;
            default:
                player.sendMessage("Valor inesperado para menu_player: " + menu_player);
                break;
        }
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

    public static void exitGui(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();

        PlayerDataGui data = DatGui.get(uuid);
        if (data != null && data.isEditing()) {
            destroyBlock(player);

            final ItemStack[] savedInventory = data.getPlayerInventories();
            if (savedInventory != null) player.getInventory().setContents(savedInventory);

            DatGui.remove(uuid);

            final String name_map = data.getMapPlayer();
            if (name_map != null && !name_map.isEmpty()) {
                for (Type type : Type.values()) {
                    HOLOGRAM_API.hideHolograms(player, name_map, type);
                }
            }

            DynamicTools.getUniquePlayerItem().remove(player);
            InventoryCache.removeInventory(uuid);

            SoundApi.playSound(player, 1.0f, 1.0f, "NOTE_BASS", "BLOCK_NOTE_BLOCK_BASS");
        }

        if (DatGui.isEmpty() && isActiveListener) {
            HandlerList.unregisterAll(GUI_LISTENER);
            if (ApiCompatibility.HAS_OFF_HAND_METHOD()) HandlerList.unregisterAll(MODERN_GUI_LISTENER);
            isActiveListener = false;
        }
    }

    public static boolean isInEditMode(final @NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        PlayerDataGui data = DatGui.get(uuid);
        return data != null && data.isEditing();
    }
}