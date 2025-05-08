package org.astral.parkour_plugin.compatibilizer.adapters;

import org.astral.parkour_plugin.compatibilizer.ApiCompatibility;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class TeleportingApi {
    public static void teleport(final Player player, final Location location){
        if (ApiCompatibility.IS_FOLIA()){
            try {
                @SuppressWarnings("JavaReflectionMemberAccess") Method teleportAsyncMethod = Player.class.getMethod("teleportAsync", Location.class);
                teleportAsyncMethod.invoke(player, location);
            } catch (Exception e) {
                player.sendMessage("§cNo se pudo usar teleportAsync. Checa Estructura");
            }
        }else {
            player.teleport(location);
        }
    }
}