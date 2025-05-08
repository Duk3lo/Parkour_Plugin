package org.astral.parkour_plugin.config.cache;

import com.google.gson.*;
import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BlockCache {
    private static final Main plugin = Main.getInstance();
    private static final File jsonFile = new File(Configuration.FOLDER_PLUGIN, Configuration.CACHE + File.separator + CacheType.Block.name() + Configuration.JSON);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, Map<Material[], Location>> mapCacheBlocks = new HashMap<>();

    private static void ensureDirectoryExists() {
        File parentDir = jsonFile.getParentFile();
        if (!parentDir.exists()) {
            if (parentDir.mkdirs()) {
                plugin.getLogger().info("Se creó la carpeta: " + parentDir.getPath());
            } else {
                plugin.getLogger().severe("No se pudo crear la carpeta: " + parentDir.getPath());
            }
        }
    }

    static {
        ensureDirectoryExists();
        if (jsonFile.exists()) {
            try (final Reader reader = new FileReader(jsonFile)) {
                final JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
                for (JsonElement element : jsonArray) {
                    final JsonObject obj = element.getAsJsonObject();
                    final UUID uuid = UUID.fromString(obj.get("id").getAsString());
                    final Material material1 = Material.valueOf(obj.get("material_1").getAsString());
                    final Material material2 = Material.valueOf(obj.get("material_2").getAsString());
                    final JsonObject locationObj = obj.getAsJsonObject("location");
                    final String worldName = locationObj.get("world").getAsString();
                    final double x = locationObj.get("x").getAsDouble();
                    final double y = locationObj.get("y").getAsDouble();
                    final double z = locationObj.get("z").getAsDouble();
                    final Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z);

                    final Map<Material[], Location> blockData = new HashMap<>();
                    blockData.put(new Material[]{material1, material2}, location);
                    mapCacheBlocks.put(uuid, blockData);
                }
                plugin.getLogger().info("Block cache initialized from JSON.");
            } catch (IOException e) {
                plugin.getLogger().severe("Error initializing block cache: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Error parsing material or location: " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("No cache file found. Starting with empty block cache.");
        }
    }

    public static void createOrUpdateOneBlockCache(final @NotNull UUID uuid, final Material material, final @NotNull Location loc) {
        createOrUpdateOneBlockCache(uuid, Material.AIR, material, loc);
    }

    public static void createOrUpdateOneBlockCache(final @NotNull UUID uuid, final Material material1, final @NotNull Material material2, final @NotNull Location loc) {
        final String id = uuid.toString();


        final Map<Material[], Location> blockData = new HashMap<>();
        blockData.put(new Material[]{material1, material2}, loc);
        mapCacheBlocks.put(uuid, blockData);

        JsonArray jsonArray = new JsonArray();
        if (jsonFile.exists()) {
            try (Reader reader = new FileReader(jsonFile)) {
                jsonArray = gson.fromJson(reader, JsonArray.class);
            } catch (IOException e) {
                plugin.getLogger().severe("Error reading JSON file: " + e.getMessage());
            }
        }

        for (JsonElement element : jsonArray) {
            final JsonObject obj = element.getAsJsonObject();
            if (obj.get("id").getAsString().equals(id)) {
                obj.addProperty("material_1", material1.name());
                obj.addProperty("material_2", material2.name());

                JsonObject location = obj.getAsJsonObject("location");
                location.addProperty("world", loc.getWorld().getName());
                location.addProperty("x", loc.getX());
                location.addProperty("y", loc.getY());
                location.addProperty("z", loc.getZ());

                saveToFile(jsonArray);
                return;
            }
        }

        final JsonObject newObject = new JsonObject();
        newObject.addProperty("id", id);
        newObject.addProperty("material_1", material1.name());
        newObject.addProperty("material_2", material2.name());

        final JsonObject location = new JsonObject();
        location.addProperty("world", loc.getWorld().getName());
        location.addProperty("x", loc.getX());
        location.addProperty("y", loc.getY());
        location.addProperty("z", loc.getZ());
        newObject.add("location", location);

        jsonArray.add(newObject);
        saveToFile(jsonArray);
    }

    public static void deleteByIdOneBlockCache(final @NotNull UUID uuid) {
        mapCacheBlocks.remove(uuid);
        if (jsonFile.exists()) {
            JsonArray jsonArray;
            try (Reader reader = new FileReader(jsonFile)) {
                jsonArray = gson.fromJson(reader, JsonArray.class);
            } catch (IOException e) {
                plugin.getLogger().severe("Error reading JSON file: " + e.getMessage());
                return;
            }

            JsonArray updatedJsonArray = new JsonArray();
            for (JsonElement element : jsonArray) {
                final JsonObject obj = element.getAsJsonObject();
                if (!obj.get("id").getAsString().equals(uuid.toString())) {
                    updatedJsonArray.add(obj);
                }
            }

            if (updatedJsonArray.size() == 0) {
                if (jsonFile.delete()) {
                    plugin.getLogger().info("JSON file deleted as it became empty.");
                } else {
                    plugin.getLogger().severe("Failed to delete the empty JSON file.");
                }
            } else {
                saveToFile(updatedJsonArray);
            }
        }
    }

    public static Map<UUID, Map<Material[], Location>> cacheTempBlock() {
        return mapCacheBlocks;
    }

    private static void saveToFile(final JsonArray jsonArray) {
        try (Writer writer = new FileWriter(jsonFile)) {
            gson.toJson(jsonArray, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving JSON file: " + e.getMessage());
        }
    }
}
