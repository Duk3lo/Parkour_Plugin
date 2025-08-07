package org.astral.parkour_plugin.gui.tools;

import org.astral.parkour_plugin.compatibilizer.adapters.MaterialApi;
import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.astral.parkour_plugin.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public enum BooleanTools {

    DESTROY_BLOCKS_EDIT_MODE(Config.romperBloques, Config.getBreakBlocksEditMode(), 0),
    COPY_BLOCKS_EDIT_MODE(Config.copiarBloque, Config.getCopyBlocks(), 0),
    SET_FLOATING_BLOCKS(Config.bloquesFlotantes, Config.getFloatingBlocks(), 0),

    ;

    private final String name;
    private boolean active;
    private int slot;

    BooleanTools(final String name, boolean active, int slot){
        this.name = name;
        this.active = active;
        this.slot = slot;
    }

    public void setSlot(final int slot) {
        this.slot = slot;
    }
    public final int getSlot(){
        return this.slot;
    }


    public @NotNull ItemStack getItem() {
        return getItem(MaterialApi.getMaterial("WOOL", "GREEN_WOOL"), MaterialApi.getMaterial("WOOL", "RED_WOOL"));
    }

    public @NotNull ItemStack getItem(final Material activate, final Material disabled) {
        final ItemStack result;
        Material baseMaterial = this.active ? activate : disabled;
        if (ApiCompatibility.ARRAY_VERSION()[1] <= 12) {
            final byte colorData = (byte) (this.active ? 5 : 14);
            if (baseMaterial == MaterialApi.getMaterial("WOOL")) {
                result = new ItemStack(baseMaterial, 1, colorData);
            } else if (baseMaterial == MaterialApi.getMaterial("CARPET")){
                result = new ItemStack(baseMaterial, 1, colorData);
            } else {
                result = new ItemStack(baseMaterial);
            }
        } else {
            result = new ItemStack(baseMaterial);
        }
        final ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((this.active ? ChatColor.GREEN : ChatColor.RED) + this.name);
            meta.setLore(Collections.singletonList(
                    (this.active ? ChatColor.GREEN : ChatColor.RED) + (this.active ? "Activado" : "Desactivado")
            ));
            result.setItemMeta(meta);
        }
        return result;
    }

    public void setItemSlot(final @NotNull Inventory inventory){
        inventory.setItem(this.slot, this.getItem());
    }

    public void toggle() {
        this.active = !this.active;
        switch (this.name){
            case Config.romperBloques:
                Config.setBreakBlocksEditMode(active);
                break;
            case Config.copiarBloque:
                Config.setCopyBlocks(active);
                break;
            case Config.bloquesFlotantes:
                Config.setFloatingBlocks(active);
                break;
            default:
                break;
        }
    }

    public boolean getToggle(){
        return this.active;
    }
}