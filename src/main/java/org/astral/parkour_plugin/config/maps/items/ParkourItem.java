package org.astral.parkour_plugin.config.maps.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ParkourItem {
    private final Material material;
    private final String displayName;
    private final List<String> lore;

    private final int slot;         // slot en el inventario
    private final int cooldown;     // cooldown en segundos
    private final int uses;         // número de usos
    private final int jumps;        // saltos extra
    private final double force;     // fuerza del impulso
    private final double upward;    // fuerza vertical
    private final ParkourItemType parkourItemType;
    private boolean giveToPlayer;

    public ParkourItem(Material material, String displayName, List<String> lore,
                       int slot, int uses, int jumps, double force, double upward,
                       int cooldown, ParkourItemType parkourItemType) {
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.slot = slot;
        this.uses = uses;
        this.jumps = jumps;
        this.force = force;
        this.upward = upward;
        this.cooldown = cooldown;

        this.parkourItemType = parkourItemType;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getSlot() {
        return slot;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getUses() {
        return uses;
    }

    public int getJumps() {
        return jumps;
    }

    public double getForce() {
        return force;
    }

    public double getUpward() {
        return upward;
    }

    public @NotNull ItemStack toItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ParkourItemType getParkourItemType() {
        return parkourItemType;
    }

    public boolean isGiveToPlayer() {
        return giveToPlayer;
    }

    public void setGiveToPlayer(boolean giveToPlayer) {
        this.giveToPlayer = giveToPlayer;
    }

}