package io.github.nekosora.nlink.network.packet.toServer.player;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.plugin.NLinkPluginManager;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.plugin.NLinkPlugin;
import io.github.nekosora.nlink.utils.CommandSenderUtils;
import io.github.nekosora.nlink.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;

import java.util.*;

import static io.github.nekosora.nlink.NLink.logger;

public class ServerboundQueryPlayerInformationPacket extends NLinkNetworkPacket {
    private final String query;
    private final String queryType; // "exact" | "uuid" | "fuzzy"
    private final boolean includeOffline;
    private final UUID query_id;

    public ServerboundQueryPlayerInformationPacket(String query, String queryType, boolean includeOffline, UUID query_id, WebSocket from) {
        super(from);
        this.query = query;
        this.queryType = queryType;
        this.includeOffline = includeOffline;
        this.query_id = query_id;
    }

    @Override
    public void handle() {
        List<Player> onlinePlayers = new ArrayList<>();
        List<OfflinePlayer> offlinePlayers = new ArrayList<>();
        List<JsonObject> resultList = new ArrayList<>();
        boolean isUUID = queryType != null && queryType.equalsIgnoreCase("uuid");
        boolean isFuzzy = queryType != null && queryType.equalsIgnoreCase("fuzzy");
        boolean isExact = queryType == null || queryType.equalsIgnoreCase("exact");

        if (isUUID) {
            try {
                UUID uuid = UUID.fromString(query);
                Player online = Bukkit.getPlayer(uuid);
                if (online != null) onlinePlayers.add(online);
                else if (includeOffline) {
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
                    offlinePlayers.add(offline);
                }
            } catch (Exception ignored) {}
        } else if (isFuzzy) {
            String lower = query.toLowerCase();
            onlinePlayers.addAll(Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.getName().toLowerCase().contains(lower))
                    .toList());
            if (includeOffline) {
                for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                    if (op.getName() != null && op.getName().toLowerCase().contains(lower)) {
                        offlinePlayers.add(op);
                    }
                }
            }
        } else if (isExact) {
            Player online = Bukkit.getPlayerExact(query);
            if (online != null) onlinePlayers.add(online);
            else if (includeOffline) {
                for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                    if (op.getName() != null && op.getName().equalsIgnoreCase(query)) {
                        offlinePlayers.add(op);
                    }
                }
            }
        }

        for (Player player : onlinePlayers) {
            JsonObject json = CommandSenderUtils.convertCommandSenderOrPlayerToJson(player);
            json.addProperty("is_online", true);
            resultList.add(json);
        }
        for (OfflinePlayer op : offlinePlayers) {
            resultList.add(offlinePlayerToJson(op));
        }

        NLinkPlugin plugin =  NLinkPluginManager.getInstance().getPlugin(getFrom());
        if (plugin == null) {
            logger.warning("Plugin not found on query_player_response");
            return;
        }

        ClientboundGenericAckPacket ackPacket = new ClientboundGenericAckPacket(
                "query_player_response",
                0,
                "Query Successful",
                plugin.getId(),
                null
        );
        ackPacket.addExtraData("query_id", query_id)
                .addExtraData("results", resultList);
        ackPacket.sendTo(getFrom());
    }

    private JsonObject offlinePlayerToJson(OfflinePlayer op) {
        JsonObject obj = new JsonObject();
        obj.addProperty("uuid", op.getUniqueId().toString());
        obj.addProperty("name", op.getName());
        obj.addProperty("is_online", false);
        return obj;
    }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        map.put("query", query);
        map.put("query_type", queryType);
        map.put("include_offline", includeOffline);
        map.put("query_id", query_id);
        return Utils.GSON.toJson(map);
    }

    @Override
    public String getPacketId() {
        return "query_player_information";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public String getQuery() { return query; }
    public String getQueryType() { return queryType; }
    public boolean isIncludeOffline() { return includeOffline; }
    public UUID getQuery_id() { return query_id; }

    public static ServerboundQueryPlayerInformationPacket fromJson(JsonObject jsonObject, WebSocket from) {
        String query = jsonObject.get("query").getAsString();
        String queryType = jsonObject.has("query_type") ? jsonObject.get("query_type").getAsString() : "exact";
        boolean includeOffline = jsonObject.has("include_offline") && jsonObject.get("include_offline").getAsBoolean();
        UUID query_id = UUID.fromString(jsonObject.get("query_id").getAsString());
        return new ServerboundQueryPlayerInformationPacket(query, queryType, includeOffline, query_id, from);
    }
}
