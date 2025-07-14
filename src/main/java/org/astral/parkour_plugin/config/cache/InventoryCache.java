package org.astral.parkour_plugin.config.cache;

import com.google.gson.*;
import org.astral.parkour_plugin.config.Config;
import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.Main;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InventoryCache {
    private static final Main plugin = Main.getInstance();
    private static final File jsonFile = new File(Configuration.FOLDER_PLUGIN,
            Configuration.CACHE + File.separator + CacheType.Inventory.name() + Configuration.JSON);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, ItemStack[]> playerInventories = new HashMap<>();
    private static final boolean DEBUG_LOGS = Config.isDebugMode();

    static {
        ensureDirectoryExists();
        loadFromFile();
    }

    private static void ensureDirectoryExists() {
        File parentDir = jsonFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            if (DEBUG_LOGS) plugin.getLogger().severe("No se pudo crear la carpeta: " + parentDir.getPath());
        }
    }

    private static void loadFromFile() {
        if (!jsonFile.exists()) return;

        try (Reader reader = new FileReader(jsonFile)) {
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
            for (JsonElement element : jsonArray) {
                JsonObject obj = element.getAsJsonObject();
                UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                ItemStack[] items = deserializeItems(obj.get("items").getAsString());
                playerInventories.put(uuid, items);
            }
            if (DEBUG_LOGS) plugin.getLogger().info("Inventarios cargados: " + playerInventories.size());
        } catch (Exception e) {
            if (DEBUG_LOGS) plugin.getLogger().severe("Error al cargar cache: " + e.getMessage());
        }
    }

    public static void saveInventory(@NotNull UUID uuid, @NotNull ItemStack[] items) {
        playerInventories.put(uuid, items);
        saveToFile();
    }

    public static ItemStack[] getInventory(@NotNull UUID uuid) {
        return playerInventories.get(uuid);
    }

    public static void removeInventory(@NotNull UUID uuid) {
        playerInventories.remove(uuid);
        saveToFile();
    }

    public static boolean hasInventory(@NotNull UUID uuid) {
        return playerInventories.containsKey(uuid);
    }

    public static Map<UUID, ItemStack[]> getAllPlayerInventories (){
        return playerInventories;
    }

    private static @NotNull String serializeItems(ItemStack @NotNull [] items) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             BukkitObjectOutputStream bois = new BukkitObjectOutputStream(bos)) {

            bois.writeInt(items.length);
            for (ItemStack item : items) {
                bois.writeObject(item);
            }
            return Base64Coder.encodeLines(bos.toByteArray());
        }
    }

    private static ItemStack @NotNull [] deserializeItems(String data) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64Coder.decodeLines(data));
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bis)) {

            ItemStack[] items = new ItemStack[bois.readInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) bois.readObject();
            }
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Clase no encontrada", e);
        }
    }

    private static void saveToFile() {
        // Si no hay inventarios guardados, eliminar el archivo
        if (playerInventories.isEmpty()) {
            if (jsonFile.exists() && !jsonFile.delete()) {
                if (DEBUG_LOGS) plugin.getLogger().warning("No se pudo eliminar el archivo de cache vacío");
            }
            return;
        }

        // Si hay datos, guardarlos
        JsonArray jsonArray = new JsonArray();
        playerInventories.forEach((uuid, items) -> {
            try {
                JsonObject entry = new JsonObject();
                entry.addProperty("uuid", uuid.toString());
                entry.addProperty("items", serializeItems(items));
                jsonArray.add(entry);
            } catch (IOException e) {
                if (DEBUG_LOGS) plugin.getLogger().warning("Error al serializar items de " + uuid);
            }
        });

        try (Writer writer = new FileWriter(jsonFile)) {
            gson.toJson(jsonArray, writer);
        } catch (IOException e) {
            if (DEBUG_LOGS) plugin.getLogger().severe("Error al guardar cache: " + e.getMessage());
        }
    }
}