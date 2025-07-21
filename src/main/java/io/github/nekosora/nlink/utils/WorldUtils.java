package io.github.nekosora.nlink.utils;

import com.google.gson.JsonObject;
import org.bukkit.World;

public class WorldUtils {
    public static JsonObject convertWorldToJson(World world) {
        JsonObject json = new JsonObject();
        json.addProperty("full_time", world.getFullTime());
        json.addProperty("name", world.getName());
        json.addProperty("player_count", world.getPlayerCount());
        return json;
    }
}