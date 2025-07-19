package io.github.nekosora.nlink.network.packet.toServer.api.player;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.NLinkPluginManager;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.plugin.NLinkPlugin;
import io.github.nekosora.nlink.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.github.nekosora.nlink.NLink.logger;

public class ServerboundQueryPlayerInformationWithPlayerName extends NLinkNetworkPacket {
    private final String playerName;
    private final UUID query_id;

    public ServerboundQueryPlayerInformationWithPlayerName(String playerName, UUID query_id, WebSocket from) {
        super(from);
        this.playerName = playerName;
        this.query_id = query_id;
    }

    @Override
    public void handle() {
        Player player = Bukkit.getPlayer(playerName);

        NLinkPlugin plugin =  NLinkPluginManager.getInstance().getPlugin(getFrom());
        if (plugin == null) {
            logger.warning("Plugin not found on query_player_response");
            return;
        }

        if (player == null) {
            ClientboundGenericAckPacket ackPacket = new ClientboundGenericAckPacket(
                    "query_player_response",
                    1,
                    "Player not found",
                    plugin.getId(),
                    null
            );

            ackPacket.addExtraData("query_id", query_id);

            ackPacket.sendTo(getFrom());
            return;
        }

        ClientboundGenericAckPacket ackPacket = new ClientboundGenericAckPacket(
                "query_player_response",
                0,
                "Query Successful",
                plugin.getId(),
                null
        );

        ackPacket
                .addExtraData("query_id", query_id)
                .addExtraData("uuid", player.getUniqueId())
                .addExtraData("x", player.getLocation().getX())
                .addExtraData("y", player.getLocation().getY())
                .addExtraData("z", player.getLocation().getZ())
                .addExtraData("world", player.getWorld().getName())
                .addExtraData("yaw", player.getLocation().getYaw())
                .addExtraData("pitch", player.getLocation().getPitch())
                .addExtraData("game_mode", player.getGameMode().name())
                .addExtraData("is_op", player.isOp())
                .addExtraData("is_flying", player.isFlying())
                .addExtraData("is_sneaking", player.isSneaking())
                .addExtraData("is_sprinting", player.isSprinting());

        ackPacket.sendTo(getFrom());
    }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        map.put("player_name", playerName);
        map.put("query_id", query_id);
        return Utils.GSON.toJson(map);
    }

    @Override
    public String getPacketId() {
        return "query_player_information_with_player_name";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getQuery_id() {
        return query_id;
    }

    public static ServerboundQueryPlayerInformationWithPlayerName fromJson(JsonObject jsonObject, WebSocket from) {
        String playerName = jsonObject.get("player_name").getAsString();
        UUID query_id = UUID.fromString(jsonObject.get("query_id").getAsString());
        return new ServerboundQueryPlayerInformationWithPlayerName(playerName, query_id, from);
    }
}
