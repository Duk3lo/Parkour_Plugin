package org.astral.parkour_plugin.editor.tools;

import org.astral.parkour_plugin.config.Config;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public enum StateTools {
    DISTANCE_BLOCK(Config.distaciaBloqueAire, Config.getDistanceBlockAir(), 0,0);

    private final String name;
    private int value;
    private int slot;
    private int page;


    StateTools(final String name, final int value, final int slot, final int page){
        this.name = name;
        this.value = value;
        this.slot = slot;
        this.page = page;
    }

    public void setSlot(final int slot) {
        this.slot = slot;
    }
    public final int getSlot(){return this.slot;}

    public void setPage(final int page) {this.page = page;}
    public int getPage(){ return this.page; }

    public int getValue() {
        return value;
    }

    public void setItemSlot(final @NotNull PlayerInventory playerInventory){
        playerInventory.setItem(this.slot, this.getItem());
    }

    public @NotNull ItemStack getItem() {

        final Material material = Material.BRICK;
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name + " §7[" + (value) + "]");
            item.setItemMeta(meta);
        }
        if (value > 1) item.setAmount(value);
        return item;
    }

    public void nextState(final int max) {
        value++;
        if (value == max) value = 0;
        if (this.name.equals(Config.distaciaBloqueAire)) {
            Config.setDistanceBlockAir(value);
        }
    }

}
