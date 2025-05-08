package org.astral.parkour_plugin.gui.tools;


import org.astral.parkour_plugin.compatibilizer.adapters.MaterialApi;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Tools {
    ADD_MAP_ITEM(MaterialApi.getMaterial("OAK_SIGN","SIGN"), ChatColor.GREEN + "Crea un Mapa",
            Collections.singletonList(ChatColor.GREEN + "Crear Mapa")),

    REMOVE_MAP(Material.MELON_SEEDS, ChatColor.RED + "Elimina este Mapa",
            Collections.singletonList(ChatColor.RED + "Esta accion es irrevercible")),

    CHECKPOINT_MARKER(Material.STICK, ChatColor.GOLD + "Checkpoint Marker",
            Arrays.asList(
                    ChatColor.YELLOW + "Click Izquierdo para",
                    ChatColor.YELLOW + "marcar checkpoints",
                    ChatColor.BLUE + "Click Derecho para",
                    ChatColor.BLUE + "desmarcar checkpoints")),

    REORDER_CHECKPOINTS(Material.BONE, ChatColor.AQUA + "Reordenar Checkpoints",
            Arrays.asList(
                    ChatColor.WHITE + "Si Eliminastes Checkpoints",
                    ChatColor.WHITE + "y quieres Actualizar los Nombres",
                    ChatColor.WHITE + "Utiliza esto para Refactorizarlos")),

    NEXT_PAGE_ITEM(Material.ARROW, ChatColor.GOLD + "Siguiente Pagina",
            Collections.singletonList(ChatColor.BLUE + "Siguiente Mapa de Checkpoints")),

    PREVIOUS_PAGE_ITEM(Material.ARROW, ChatColor.GOLD + "Anterior Pagina",
            Collections.singletonList(ChatColor.BLUE + "Anterior Mapa de Checkpoints")),

    EDIT_FEATHER_ITEM(Material.FEATHER, ChatColor.DARK_PURPLE + "Editar", null),

    CHANGE_ITEM_POSITION(Material.BONE, ChatColor.DARK_PURPLE + "Cambiar Posiciones", null),

    EXIT_ITEM(Material.BARRIER, ChatColor.RED + "Cancelar Cambios", null),

    APPLY_CHANGES_ITEM(Material.ANVIL, ChatColor.GREEN + "Aplicar Cambios", null),

    CANCEL_CHANGES_ITEM(Material.REDSTONE, ChatColor.RED + "Cancelar Cambios", null),

    BACK_ITEM(Material.REDSTONE, ChatColor.RED + "Volver", null),

    GENERATION_BLOCK(Material.STONE, ChatColor.GOLD + "Block Generator",
            Arrays.asList(
                    ChatColor.YELLOW + "Click Izquierdo para",
                    ChatColor.YELLOW + "crear un ejemplo de checkpoint")),

    OPEN_INVENTORY_ITEM(Material.BEACON, ChatColor.GREEN + "Opciones Generales", null),

    SPAWN_AND_FINISH_MENU(Material.BLAZE_ROD, ChatColor.GOLD+ "Editor de Spawns y Finalizacion",
            Collections.singletonList(ChatColor.YELLOW + "Establece el spawn de los jugadores y su meta final")),

    ONE_CHECKPOINT_MENU(Material.STICK, ChatColor.WHITE + "Menu de Checkpoints",
            Collections.singletonList(ChatColor.AQUA + "Establece un Checkpoint")),

    LIST_CHECKPOINT_MENU(Material.STICK, ChatColor.LIGHT_PURPLE + "Menu de Varios Checkpoints en uno",
            Collections.singletonList(ChatColor.AQUA + "Establece varios checkpoints como uno")),

    MARK_SPAWN_ITEM(Material.BLAZE_ROD, ChatColor.GOLD+"Marca el Spawn de los jugadores", null),

    MARK_FINISH_ITEM(Material.BLAZE_ROD, ChatColor.WHITE+"Marca la meta final", null),
    ;

    private final ItemStack item;

    Tools(final Material material, final String displayName, final List<String> lore) {
        this.item = new ItemStack(material);
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (lore != null) {
                meta.setLore(lore);
            }
            this.item.setItemMeta(meta);
        }
    }

    @NotNull
    public ItemStack getItem() {
        return this.item.clone();
    }
}