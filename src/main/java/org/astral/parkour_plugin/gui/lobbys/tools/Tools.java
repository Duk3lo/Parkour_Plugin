package org.astral.parkour_plugin.gui.lobbys.tools;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum Tools {

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
