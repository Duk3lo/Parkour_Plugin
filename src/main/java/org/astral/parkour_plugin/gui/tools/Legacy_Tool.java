package org.astral.parkour_plugin.gui.tools;

import org.astral.parkour_plugin.compatibilizer.adapters.MaterialApi;
import org.astral.parkour_plugin.config.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.config.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Deprecated
public final class Legacy_Tool {

    //// --------------------------------------------------------------------------[INIT]
    //// --------------------------------------------------------------------------------
    //// [EDITOR]
    public static final ItemStack ADD_MAP_ITEM;
    public static final ItemStack REMOVE_MAP;
    public static final List<ItemStack> SELECTS_MAPS_ITEMS = new ArrayList<>();
    //public static final List<ItemStack> CHECKPOINTS_MAPS_ITEMS = new ArrayList<>();
    public static final Map<String, List<ItemStack>> CHECKPOINTS_MAPS_ITEMS = new HashMap<>();
    public static final ItemStack CHECKPOINT_MARKER;
    public static final ItemStack REORDER_CHECKPOINTS;
    public static final ItemStack NEXT_PAGE_ITEM;
    public static final ItemStack PREVIOUS_PAGE_ITEM;
    public static final ItemStack EDIT_FEATHER_ITEM;
    public static final ItemStack CHANGE_ITEM_POSITION;
    public static final ItemStack EXIT_ITEM;
    public static final ItemStack APPLY_CHANGES_ITEM;
    public static final ItemStack CANCEL_CHANGES_ITEM;
    public static final ItemStack BACK_ITEM;

    //// [OTHERS]
    public static final ItemStack GENERATION_BLOCK;

    static {
        //// ------------------------------------------------------------------------[EDITOR]
        //// --------------------------------------------------------------------------------
        final ItemStack addMap = new ItemStack(MaterialApi.getMaterial("OAK_SIGN","SIGN"));
        final ItemMeta addMapMeta = addMap.getItemMeta();
        if (addMapMeta != null) {
            addMapMeta.setDisplayName(ChatColor.GREEN + "Crea un Mapa");
            addMapMeta.setLore(Collections.singletonList(
                    ChatColor.GREEN + "Crear Mapa"
            ));
            //checkpointMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            addMap.setItemMeta(addMapMeta);
            //addMap.addUnsafeEnchantment(Enchantment.KNOCKBACK, 0);
        }
        ADD_MAP_ITEM = addMap;
        final ItemStack removeMap = new ItemStack(Material.MELON_SEEDS);
        final ItemMeta removeMapMeta = removeMap.getItemMeta();
        if (removeMapMeta != null) {
                removeMapMeta.setDisplayName(ChatColor.RED + "Elimina este Mapa");
            removeMapMeta.setLore(Collections.singletonList(
                    ChatColor.RED + "Esta accion es irrevercible"
            ));
            //checkpointMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            removeMap.setItemMeta(removeMapMeta);
            //removeMap.addEnchantment(Enchantment.KNOCKBACK, 0);
        }
        REMOVE_MAP = removeMap;

        ////------------------------------------------------------------------------------[INTERFACE]
        ////-----------------------------------------------------------------------------------------
        final ItemStack checkpointItem = new ItemStack(Material.STICK);
        final ItemMeta checkpointMeta = checkpointItem.getItemMeta();
        if (checkpointMeta != null) {
            checkpointMeta.setDisplayName(ChatColor.GOLD + "Checkpoint Marker");
            checkpointMeta.setLore(Arrays.asList(
                    ChatColor.YELLOW + "Click Izquierdo para",
                    ChatColor.YELLOW + "marcar checkpoints",
                    ChatColor.BLUE + "Click Derecho para",
                    ChatColor.BLUE + "desmarcar checkpoints"
            ));
            //checkpointMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            checkpointItem.setItemMeta(checkpointMeta);
            //checkpointItem.addEnchantment(Enchantment.KNOCKBACK, 0);
        }
        CHECKPOINT_MARKER = checkpointItem;

        final ItemStack reorderItem = new ItemStack(Material.BONE);
        final ItemMeta reorderMeta = reorderItem.getItemMeta();
        if (reorderMeta != null) {
            reorderMeta.setDisplayName(ChatColor.AQUA + "Reordenar Checkpoints");
            reorderMeta.setLore(Arrays.asList(
                    ChatColor.WHITE + "Si Eliminastes Checkpoints",
                    ChatColor.WHITE + "y quieres Actualizar los Nombres",
                    ChatColor.WHITE + "Utiliza esto para Refactorizarlos"
            ));
            //checkpointMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            reorderItem.setItemMeta(reorderMeta);
            //reorderItem.addEnchantment(Enchantment.KNOCKBACK, 0);
        }
        REORDER_CHECKPOINTS = reorderItem;

        final ItemStack nextPageItem = new ItemStack(Material.ARROW);
        final ItemMeta nextPageItemMeta = nextPageItem.getItemMeta();
        if (nextPageItemMeta != null){
            nextPageItemMeta.setDisplayName(ChatColor.GOLD + "Siguiente Pagina");
            nextPageItemMeta.setLore(Collections.singletonList(
                    ChatColor.BLUE + "Siguiente Mapa de Checkpoints"
            ));
            //nextPageItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            nextPageItem.setItemMeta(nextPageItemMeta);
            //nextPageItem.addEnchantment(Enchantment.KNOCKBACK,0);
        }
        NEXT_PAGE_ITEM = nextPageItem;

        final ItemStack previousPageItem = new ItemStack(Material.ARROW);
        final ItemMeta previousPageItemItemMeta = previousPageItem.getItemMeta();
        if (previousPageItemItemMeta != null){
            previousPageItemItemMeta.setDisplayName(ChatColor.GOLD + "Anterior Pagina");
            previousPageItemItemMeta.setLore(Collections.singletonList(
                    ChatColor.BLUE + "Anterior Mapa de Checkpoints"
            ));
            //previousPageItemItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            previousPageItem.setItemMeta(previousPageItemItemMeta);
            //previousPageItem.addUnsafeEnchantment(Enchantment.KNOCKBACK,0);
        }
        PREVIOUS_PAGE_ITEM = previousPageItem;


        ////-------------------------------------------------------------[REPLACE-EDITOR-DELETE-ADD]
        ////----------------------------------------------------------------------------------------

        final ItemStack editMode = new ItemStack(Material.FEATHER);
        final ItemMeta editModeMeta = editMode.getItemMeta();
        if (editModeMeta != null){
            editModeMeta.setDisplayName(ChatColor.DARK_PURPLE + "Editar");
            //editModeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            editMode.setItemMeta(editModeMeta);
            //editMode.addEnchantment(Enchantment.KNOCKBACK,0);
        }
        EDIT_FEATHER_ITEM = editMode;

        final ItemStack changePositions = new ItemStack(Material.BONE);
        final ItemMeta changePositionsMeta = changePositions.getItemMeta();
        if (changePositionsMeta != null){
            changePositionsMeta.setDisplayName(ChatColor.DARK_PURPLE + "Cambiar Posiciones");
            //changePositionsMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            changePositions.setItemMeta(changePositionsMeta);
            //changePositions.addEnchantment(Enchantment.KNOCKBACK,0);
        }
        CHANGE_ITEM_POSITION = changePositions;

        final ItemStack removeItem = new ItemStack(Material.BARRIER);
        final ItemMeta removeItemMeta = removeItem.getItemMeta();
        if (removeItemMeta != null){
            removeItemMeta.setDisplayName(ChatColor.RED + "Cancelar Cambios");
            //removeItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            removeItem.setItemMeta(removeItemMeta);
            //removeItem.addEnchantment(Enchantment.KNOCKBACK,0);
        }
        EXIT_ITEM = removeItem;


        final ItemStack applyItem = new ItemStack(Material.ANVIL);
        final ItemMeta applyItemMeta = applyItem.getItemMeta();
        if (applyItemMeta != null){
            applyItemMeta.setDisplayName(ChatColor.GREEN + "Aplicar Cambios");
            //applyItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            applyItem.setItemMeta(applyItemMeta);
            //applyItem.addEnchantment(Enchantment.KNOCKBACK,0);
        }
        APPLY_CHANGES_ITEM = applyItem;

        final ItemStack cancelItem = new ItemStack(Material.REDSTONE);
        final ItemMeta cancelItemMeta = cancelItem.getItemMeta();
        if (cancelItemMeta != null){
            cancelItemMeta.setDisplayName(ChatColor.RED + "Cancelar Cambios");
            //cancelItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            cancelItem.setItemMeta(cancelItemMeta);
            //cancelItem.addEnchantment(Enchantment.KNOCKBACK,0);
        }
        CANCEL_CHANGES_ITEM = cancelItem;

        final ItemStack backItem = new ItemStack(Material.REDSTONE);
        final ItemMeta backItemMeta = backItem.getItemMeta();
        if (backItemMeta != null){
            backItemMeta.setDisplayName(ChatColor.RED + "Volver");
            //backItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            backItem.setItemMeta(backItemMeta);
            //backItem.addEnchantment(Enchantment.KNOCKBACK,0);
        }
        BACK_ITEM = backItem;

        //// --------------------------------------------------------------------------------
        //// --------------------------------------------------------------------------------
        final ItemStack generationItem = new ItemStack(Material.STONE);
        final ItemMeta generationMeta = generationItem.getItemMeta();
        if (generationMeta != null) {
            generationMeta.setDisplayName(ChatColor.GOLD + "Block Generator");
            generationMeta.setLore(Arrays.asList(
                    ChatColor.YELLOW + "Click Izquierdo para",
                    ChatColor.YELLOW + "crear un ejemplo de checkpoint"
            ));
            //generationItem.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            generationItem.setItemMeta(generationMeta);
            //generationItem.addEnchantment(Enchantment.KNOCKBACK, 0);
        }
        GENERATION_BLOCK = generationItem;

        for (final String name : sortMapNames(Configuration.getMaps())) {
            final ItemStack mapItem = new ItemStack(getRandomMaterial());
            final ItemMeta mapItemMeta = mapItem.getItemMeta();
            if (mapItemMeta != null) {
                mapItemMeta.setDisplayName(ChatColor.GREEN + name);
                mapItemMeta.setLore(Arrays.asList(
                        ChatColor.WHITE + "Carga el Mapa: " + name,
                        ChatColor.LIGHT_PURPLE + "Editar"
                ));
                //mapItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            mapItem.setItemMeta(mapItemMeta);
            //mapItem.addEnchantment(Enchantment.KNOCKBACK, 0);
            SELECTS_MAPS_ITEMS.add(mapItem);
        }
    }

    public static void refreshMaps(){
        SELECTS_MAPS_ITEMS.clear();
        for (final String name : sortMapNames(Configuration.getMaps())) {
            final ItemStack mapItem = new ItemStack(getRandomMaterial());
            final ItemMeta mapItemMeta = mapItem.getItemMeta();
            if (mapItemMeta != null) {
                mapItemMeta.setDisplayName(ChatColor.GREEN + name);
                mapItemMeta.setLore(Arrays.asList(
                        ChatColor.WHITE + "Carga el Mapa: " + name,
                        ChatColor.LIGHT_PURPLE + "Editar"
                ));
                //mapItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            mapItem.setItemMeta(mapItemMeta);
            //mapItem.addEnchantment(Enchantment.KNOCKBACK, 0);
            SELECTS_MAPS_ITEMS.add(mapItem);
        }
    }

    public static @NotNull ItemStack createItemMap(final String name){
        final ItemStack mapItem = new ItemStack(getRandomMaterial());
        final ItemMeta mapItemMeta = mapItem.getItemMeta();
        if (mapItemMeta != null) {
            mapItemMeta.setDisplayName(ChatColor.GREEN + name);
            mapItemMeta.setLore(Arrays.asList(
                    ChatColor.WHITE + "Carga el Mapa: " + name,
                    ChatColor.LIGHT_PURPLE + "Editar"
            ));
            //mapItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        mapItem.setItemMeta(mapItemMeta);
        //mapItem.addEnchantment(Enchantment.KNOCKBACK, 0);
        return mapItem;
    }

    //// -------------------------------------------------------------------------------[ACTIVES]
    //// ----------------------------------------------------------------------------------------



    //// -------------------------------------------------------------------------------[CHECKPOINTS]
    //// ----------------------------------------------------------------------------------------
    public static void loadCheckpointsItems(final String name){
        CHECKPOINTS_MAPS_ITEMS.computeIfAbsent(name, k -> new ArrayList<>());
        CHECKPOINTS_MAPS_ITEMS.get(name).clear();
        final CheckpointConfig config = new CheckpointConfig(name);
        for (final String checkpoint : config.keys()){
            try {
                config.getCheckpoint(checkpoint);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            final Pattern pattern = Pattern.compile("\\d+");
            final Matcher matcher = pattern.matcher(checkpoint);

            int number = 1;
            if (matcher.find()){
                number = Integer.parseInt(matcher.group());
            }
            number = Math.min(number, 64);

            final ItemStack checkpointItem = new ItemStack(Material.TORCH);
            checkpointItem.setAmount(number);
            final ItemMeta checkpointItemMeta = getItemMeta(checkpoint, checkpointItem, config);
            checkpointItem.setItemMeta(checkpointItemMeta);
            CHECKPOINTS_MAPS_ITEMS.get(name).add(checkpointItem);
        }
    }


    private static @Nullable ItemMeta getItemMeta(String checkpoint, @NotNull ItemStack checkpointItem, CheckpointConfig config) {
        final ItemMeta checkpointItemMeta = checkpointItem.getItemMeta();
        if (checkpointItemMeta != null) {
            checkpointItemMeta.setDisplayName(ChatColor.GOLD + checkpoint);
            checkpointItemMeta.setLore(Arrays.asList(
                    ChatColor.WHITE + "Vamos al: " + checkpoint,
                    ChatColor.BLUE + "x:"+ config.getLocation().getX() + " y: " + config.getLocation().getY() + " z: " + config.getLocation().getZ()
            ));
        }
        return checkpointItemMeta;
    }

    //// ----------------------------------------------------------------------------[VALIDATORS]
    //// ----------------------------------------------------------------------------------------

    public static String getName(final ItemStack itemStack) {
        if (itemStack == null) return null;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName())return ChatColor.stripColor(meta.getDisplayName());
        return null;
    }

    //// ----------------------------------------------------------------------------[COMPARATORS]
    //// -----------------------------------------------------------------------------------------
    private static @NotNull List<String> sortMapNames(final List<String> mapNames) {
        final List<String> sortedNames = new ArrayList<>(mapNames);
        sortedNames.sort((a, b) -> {
            final Pattern pattern = Pattern.compile("(\\D*)(\\d*)");
            final Matcher matcherA = pattern.matcher(a);
            final Matcher matcherB = pattern.matcher(b);

            while (matcherA.find() && matcherB.find()) {
                int textCompare = matcherA.group(1).compareTo(matcherB.group(1));
                if (textCompare != 0) {
                    return textCompare;
                }
                final String numA = matcherA.group(2);
                final String numB = matcherB.group(2);
                if (numA.isEmpty() && numB.isEmpty()) {
                    continue;
                }
                final int numberA = numA.isEmpty() ? 0 : Integer.parseInt(numA);
                final int numberB = numB.isEmpty() ? 0 : Integer.parseInt(numB);
                final int numberCompare = Integer.compare(numberA, numberB);
                if (numberCompare != 0) {
                    return numberCompare;
                }
            }
            return a.compareTo(b);
        });
        return sortedNames;
    }

    //// ----------------------------------------------------------------------------[MATERIALS]
    //// ---------------------------------------------------------------------------------------

    private static Material getRandomMaterial() {
        final List<Material> allowedMaterials = Arrays.stream(Material.values())
                .filter(material -> !material.isBlock())
                .collect(Collectors.toList());
        final Random random = new Random();
        final int randomIndex = random.nextInt(allowedMaterials.size());
        return allowedMaterials.get(randomIndex);
    }

}