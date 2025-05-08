package org.astral.parkour_plugin.config.cache;

import com.google.gson.*;
import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public final class EntityCache {
    private static final Main plugin = Main.getInstance();
    private static final File jsonFile = new File(Configuration.FOLDER_PLUGIN, Configuration.CACHE + File.separator + CacheType.Entity.name() + Configuration.JSON);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<EntityType, List<UUID>> mapCacheEntity = new HashMap<>();

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
                final JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                if (jsonObject != null) {
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        EntityType entityType = EntityType.valueOf(entry.getKey());
                        JsonArray uuidArray = entry.getValue().getAsJsonArray();
                        List<UUID> uuids = new ArrayList<>();
                        for (JsonElement uuidElement : uuidArray) {
                            uuids.add(UUID.fromString(uuidElement.getAsString()));
                        }
                        mapCacheEntity.put(entityType, uuids);
                    }
                    plugin.getLogger().info("Entity cache initialized from JSON.");
                } else {
                    plugin.getLogger().info("JSON file is empty or invalid. Starting with empty entity cache.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Error initializing entity cache: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Error parsing entity type or UUID: " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("No cache file found. Starting with empty entity cache.");
        }
    }

    public static void addEntityToCache(final @NotNull Entity entity) {
        EntityType entityType = entity.getType();
        UUID uuid = entity.getUniqueId();
        mapCacheEntity.computeIfAbsent(entityType, k -> new ArrayList<>()).add(uuid);
        saveToFile();
    }

    public static void removeEntityTypeFromCache(final @NotNull EntityType entityType) {
        if (!mapCacheEntity.containsKey(entityType)) {
            plugin.getLogger().info("EntityType " + entityType + " not found in cache.");
            return;
        }

        mapCacheEntity.remove(entityType);
        plugin.getLogger().info("EntityType " + entityType + " removed from cache.");

        saveToFile();
        if (jsonFile.exists()) {
            JsonObject jsonObject;
            try (Reader reader = new FileReader(jsonFile)) {
                jsonObject = gson.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                plugin.getLogger().severe("Error reading JSON file: " + e.getMessage());
                return;
            }

            if (jsonObject == null || jsonObject.entrySet().isEmpty()) {
                if (jsonFile.delete()) {
                    plugin.getLogger().info("JSON file deleted as it became empty.");
                } else {
                    plugin.getLogger().severe("Failed to delete the empty JSON file.");
                }
            }
        }
    }

    public static void removeEntityFromCache(final @NotNull Entity entity) {
        EntityType entityType = entity.getType();
        UUID uuid = entity.getUniqueId();
        if (!mapCacheEntity.containsKey(entityType)) {
            plugin.getLogger().info("EntityType " + entityType + " not found in cache.");
            return;
        }
        final List<UUID> uuids = mapCacheEntity.get(entityType);

        boolean removed = uuids.remove(uuid);
        if (!removed) {
            plugin.getLogger().info("UUID " + uuid + " not found for EntityType " + entityType + ".");
            return;
        }
        if (uuids.isEmpty()) {
            mapCacheEntity.remove(entityType);
        }
        if (jsonFile.exists()) {
            JsonObject jsonObject;
            try (Reader reader = new FileReader(jsonFile)) {
                jsonObject = gson.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                plugin.getLogger().severe("Error reading JSON file: " + e.getMessage());
                return;
            }

            if (jsonObject != null) { // Verificar si el JSON no es nulo
                JsonArray uuidArray = jsonObject.getAsJsonArray(entityType.name());
                if (uuidArray != null) {
                    JsonArray updatedUuidArray = new JsonArray();
                    for (JsonElement uuidElement : uuidArray) {
                        if (!uuidElement.getAsString().equals(uuid.toString())) {
                            updatedUuidArray.add(new JsonPrimitive(uuidElement.getAsString()));
                        }
                    }

                    if (updatedUuidArray.size() == 0) { // Workaround for isEmpty()
                        jsonObject.remove(entityType.name());
                    } else {
                        jsonObject.add(entityType.name(), updatedUuidArray);
                    }
                }

                if (jsonObject.entrySet().size() == 0) { // Workaround for isEmpty()
                    if (jsonFile.delete()) {
                        plugin.getLogger().info("JSON file deleted as it became empty.");
                    } else {
                        plugin.getLogger().severe("Failed to delete the empty JSON file.");
                    }
                } else {
                    try (Writer writer = new FileWriter(jsonFile)) {
                        gson.toJson(jsonObject, writer);
                    } catch (IOException e) {
                        plugin.getLogger().severe("Error saving JSON file: " + e.getMessage());
                    }
                }
            } else {
                plugin.getLogger().info("JSON file is empty or invalid. Skipping update.");
            }
        }
    }

    public static Map<EntityType, List<UUID>> getEntityCache() {
        return mapCacheEntity;
    }

    private static void saveToFile() {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<EntityType, List<UUID>> entry : mapCacheEntity.entrySet()) {
            JsonArray uuidArray = new JsonArray();
            for (UUID uuid : entry.getValue()) {
                uuidArray.add(new JsonPrimitive(uuid.toString()));
            }
            jsonObject.add(entry.getKey().name(), uuidArray);
        }

        try (Writer writer = new FileWriter(jsonFile)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving JSON file: " + e.getMessage());
        }
    }
}