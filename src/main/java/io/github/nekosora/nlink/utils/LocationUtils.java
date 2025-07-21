package io.github.nekosora.nlink.utils;

import com.google.gson.JsonObject;
import org.bukkit.Location;

public class LocationUtils {
    public static JsonObject convertLocationToJson(Location location) {
        JsonObject jsonObject = new JsonObject();
        if (location == null) {
            return jsonObject;
        }
        if (location.getWorld() != null) {
            jsonObject.add("world", WorldUtils.convertWorldToJson(location.getWorld()));
        } else {
            jsonObject.add("world", new JsonObject());
        }
        jsonObject.addProperty("x", location.getX());
        jsonObject.addProperty("y", location.getY());
        jsonObject.addProperty("z", location.getZ());
        jsonObject.addProperty("yaw", location.getYaw());
        jsonObject.addProperty("pitch", location.getPitch());
        jsonObject.addProperty("block_x", location.getBlockX());
        jsonObject.addProperty("block_y", location.getBlockY());
        jsonObject.addProperty("block_z", location.getBlockZ());
        return jsonObject;
    }
}
