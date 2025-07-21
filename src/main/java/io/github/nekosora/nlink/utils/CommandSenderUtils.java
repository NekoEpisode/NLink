package io.github.nekosora.nlink.utils;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSenderUtils {
    public static JsonObject convertCommandSenderOrPlayerToJson(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            return convertPlayerToJson(player);
        } else {
            return convertCommandSenderToJson(commandSender);
        }
    }

    private static JsonObject convertCommandSenderToJson(CommandSender commandSender) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", commandSender.getName());
        return jsonObject;
    }

    private static JsonObject convertPlayerToJson(Player player) {
        if (player == null) {
            return new JsonObject();
        }

        JsonObject json = new JsonObject();

        // 基础信息
        addBasicInfo(json, player);

        // 网络与连接信息
        addNetworkInfo(json, player);

        // 位置与移动信息
        addLocationInfo(json, player);

        // 游戏状态与属性
        addGameStats(json, player);

        // 实体状态
        addEntityStats(json, player);

        return json;
    }

    private static void addBasicInfo(JsonObject json, Player player) {
        json.addProperty("uuid", player.getUniqueId().toString());
        json.addProperty("name", player.getName());
        json.addProperty("display_name",
                PlainTextComponentSerializer.plainText().serialize(player.displayName()));
        json.addProperty("game_mode", player.getGameMode().name());
        json.addProperty("is_op", player.isOp());
    }

    private static void addNetworkInfo(JsonObject json, Player player) {
        json.addProperty("client_brand", getClientBrandSafe(player));
        json.addProperty("ping", player.getPing());
        if (player.getAddress() != null) {
            json.addProperty("ip_address", player.getAddress().getAddress().getHostAddress());
        }
    }

    private static void addLocationInfo(JsonObject json, Player player) {
        json.add("location", LocationUtils.convertLocationToJson(player.getLocation()));
        json.add("compass_target", LocationUtils.convertLocationToJson(player.getCompassTarget()));
        json.addProperty("allow_flight", player.getAllowFlight());
        json.addProperty("is_flying", player.isFlying());
        json.addProperty("walk_speed", player.getWalkSpeed());
        json.addProperty("fly_speed", player.getFlySpeed());
    }

    private static void addGameStats(JsonObject json, Player player) {
        json.addProperty("exp", player.getExp());
        json.addProperty("level", player.getLevel());
        json.addProperty("total_exp", player.getTotalExperience());
        json.addProperty("exp_to_level", player.getExpToLevel());
        json.addProperty("health", player.getHealth());
        json.addProperty("max_health", player.getMaxHealth());
        json.addProperty("health_scale", player.getHealthScale());
        json.addProperty("player_time", player.getPlayerTime());
        json.addProperty("player_time_offset", player.getPlayerTimeOffset());
        json.addProperty("allow_list_on_player_list", player.isAllowingServerListings());
        json.addProperty("affect_spawning", player.getAffectsSpawning());
        json.addProperty("view_distance", player.getViewDistance());
    }

    private static void addEntityStats(JsonObject json, Player player) {
        json.addProperty("is_sneaking", player.isSneaking());
        json.addProperty("is_sprinting", player.isSprinting());
        json.addProperty("is_gliding", player.isGliding());
        json.addProperty("is_swimming", player.isSwimming());
        json.addProperty("is_sleeping", player.isSleeping());
        json.addProperty("is_blocking", player.isBlocking());
        json.addProperty("fire_ticks", player.getFireTicks());
        json.addProperty("freeze_ticks", player.getFreezeTicks());
        json.addProperty("no_damage_ticks", player.getNoDamageTicks());
        json.addProperty("remaining_air", player.getRemainingAir());
        json.addProperty("maximum_air", player.getMaximumAir());
    }

    private static String getClientBrandSafe(Player player) {
        try {
            return player.getClientBrandName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
