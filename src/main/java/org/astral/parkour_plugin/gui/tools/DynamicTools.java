package org.astral.parkour_plugin.gui.tools;

import org.astral.parkour_plugin.config.maps.checkpoint.CheckpointConfig;
import org.astral.parkour_plugin.config.maps.rules.Rules;
import org.astral.parkour_plugin.config.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public final class DynamicTools {

    public static final List<ItemStack> SELECTS_MAPS_ITEMS = new ArrayList<>();
    public static final Map<String, List<ItemStack>> CHECKPOINTS_MAPS_ITEMS = new HashMap<>();
    public static final Map<String, List<ItemStack>> SPAWN_LOCATIONS = new HashMap<>();
    public static final Map<String, List<ItemStack>> FINISH_LOCATION = new HashMap<>();

    private static final Map<Player, ItemStack> uniquePlayerItem = new HashMap<>();

    static {
        refreshMaps();
    }

    public static void setUniquePlayerItem(final @NotNull Player player , final @NotNull ItemStack itemStack){
        uniquePlayerItem.put(player, itemStack);
    }

    public static Map<Player, ItemStack> getUniquePlayerItem(){
        return uniquePlayerItem;
    }

    public static void refreshMaps() {
        SELECTS_MAPS_ITEMS.clear();
        for (final String name : sortMapNames(Configuration.getMaps())) {
            SELECTS_MAPS_ITEMS.add(createItemMap(name));
        }
    }

    public static @NotNull ItemStack createItemMap(final String name) {
        final ItemStack mapItem = new ItemStack(getRandomMaterial());
        final ItemMeta mapItemMeta = mapItem.getItemMeta();
        if (mapItemMeta != null) {
            mapItemMeta.setDisplayName(ChatColor.GREEN + name);
            mapItemMeta.setLore(Arrays.asList(
                    ChatColor.WHITE + "Carga el Mapa: " + name,
                    ChatColor.LIGHT_PURPLE + "Editar"
            ));
        }
        mapItem.setItemMeta(mapItemMeta);
        return mapItem;
    }

    public static void loadSpawnPoints(final String name) {
        SPAWN_LOCATIONS.computeIfAbsent(name, k -> new ArrayList<>()).clear();
        final Rules rules = new Rules(name);

        for (final Location location : rules.getSpawnsPoints()) {
            final String spawnKey = rules.getSpawnKeyFromLocation(location);

            final ItemStack spawnLocationItem = new ItemStack(Material.TRIPWIRE_HOOK);
            final ItemMeta spawnMeta = spawnLocationItem.getItemMeta();
            if (spawnMeta != null) {
                spawnMeta.setDisplayName(ChatColor.AQUA + spawnKey); // ⬅ Le das ese nombre al item
                spawnMeta.setLore(Collections.singletonList(ChatColor.GREEN + "Ubicación: x=" + location.getX() + ", y=" + location.getY() + ", z=" + location.getZ()));
            }
            spawnLocationItem.setItemMeta(spawnMeta);
            SPAWN_LOCATIONS.get(name).add(spawnLocationItem);
        }
    }

    public static void loadFinishPoints(final String name) {
        FINISH_LOCATION.computeIfAbsent(name, k -> new ArrayList<>()).clear();
        final Rules rules = new Rules(name);
        for (final Location location : rules.getEndPoints()) {
            final String endKey = rules.getEndPointKeyFromLocation(location);
            final ItemStack finishItem = new ItemStack(Material.LEVER);
            final ItemMeta finishMeta = finishItem.getItemMeta();
            if (finishMeta != null) {
                finishMeta.setDisplayName(ChatColor.LIGHT_PURPLE + endKey);
                finishMeta.setLore(Collections.singletonList(ChatColor.GREEN + "Ubicación: x=" + location.getX() + ", y=" + location.getY() + ", z=" + location.getZ()));
            }
            finishItem.setItemMeta(finishMeta);
            FINISH_LOCATION.get(name).add(finishItem);
        }
    }


    public static void loadCheckpointsItems(final String name) {
        CHECKPOINTS_MAPS_ITEMS.computeIfAbsent(name, k -> new ArrayList<>()).clear();
        final CheckpointConfig config = new CheckpointConfig(name);
        for (final String checkpoint : config.keys()) {
            try {
                config.getCheckpoint(checkpoint);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            int number = extractNumberFromCheckpoint(checkpoint);
            final ItemStack checkpointItem = new ItemStack(Material.TORCH, number);
            final ItemMeta checkpointItemMeta = getItemMeta(checkpoint, checkpointItem, config);
            checkpointItem.setItemMeta(checkpointItemMeta);
            CHECKPOINTS_MAPS_ITEMS.get(name).add(checkpointItem);
        }
    }

    public static String getName(final ItemStack itemStack) {
        if (itemStack == null) return null;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) return ChatColor.stripColor(meta.getDisplayName());
        return null;
    }

    private static int extractNumberFromCheckpoint(final String checkpoint) {
        final Pattern pattern = Pattern.compile("\\d+");
        final Matcher matcher = pattern.matcher(checkpoint);
        int number = 1;
        if (matcher.find()) {
            number = Integer.parseInt(matcher.group());
        }
        return Math.min(number, 64);
    }

    private static @Nullable ItemMeta getItemMeta(final String checkpoint, final @NotNull ItemStack checkpointItem, final CheckpointConfig config) {
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

    private static Material getRandomMaterial() {
        final List<Material> allowedMaterials = Arrays.stream(Material.values())
                .filter(material -> !material.isBlock())
                .collect(Collectors.toList());
        final Random random = new Random();
        final int randomIndex = random.nextInt(allowedMaterials.size());
        return allowedMaterials.get(randomIndex);
    }

    private DynamicTools() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no puede ser instanciada");
    }
}