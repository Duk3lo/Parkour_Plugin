package org.astral.parkour_plugin.gui;


import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataGui {
    private String menu;
    private ItemStack[] playerInventories;
    private boolean editing;
    private int page;
    private String mapPlayer;
    private Map<Integer, ItemStack> originalInventory;

    public PlayerDataGui() {
        this.editing = false;
        this.page = 0;
        this.originalInventory = new HashMap<>();
    }


    public String getMenu() {
        return menu != null ? menu : "";
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public ItemStack[] getPlayerInventories() {
        return playerInventories != null ? playerInventories : new ItemStack[0];
    }

    public void setPlayerInventories(ItemStack[] playerInventories) {
        this.playerInventories = playerInventories;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getMapPlayer() {
        return mapPlayer != null ? mapPlayer : "";
    }

    public void setMapPlayer(String mapPlayer) {
        this.mapPlayer = mapPlayer;
    }

    public Map<Integer, ItemStack> getOriginalInventory() {
        return originalInventory != null ? originalInventory : Collections.emptyMap();
    }

    public void setOriginalInventory(Map<Integer, ItemStack> originalInventory) {
        this.originalInventory = originalInventory;
    }
}
