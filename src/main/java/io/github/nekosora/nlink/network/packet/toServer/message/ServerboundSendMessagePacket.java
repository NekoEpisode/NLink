package io.github.nekosora.nlink.network.packet.toServer.message;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.NLink;
import io.github.nekosora.nlink.plugin.NLinkPluginManager;
import io.github.nekosora.nlink.utils.Utils;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.plugin.NLinkPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerboundSendMessagePacket extends NLinkNetworkPacket {
    private final String targetUUID;
    private final String message;

    public ServerboundSendMessagePacket(String targetUUID, String message, WebSocket from) {
        super(from);
        this.targetUUID = targetUUID;
        this.message = message;
    }

    @Override
    public void handle() {
        if ("console".equals(targetUUID)) {
            NLinkPlugin plugin = NLinkPluginManager.getInstance().getPlugin(getFrom());
            NLink.instance.getLogger().info("[" + (plugin != null ? plugin.getId() : "Unknown plugin") + "] " + message);
            return;
        }

        Player player = null;
        // 支持UUID或玩家名
        if (targetUUID.length() == 36 && targetUUID.matches("[0-9a-fA-F\\-]+")) {
            try {
                player = Bukkit.getPlayer(UUID.fromString(targetUUID));
            } catch (Exception ignored) {}
        }
        if (player == null) {
            player = Bukkit.getPlayerExact(targetUUID);
        }
        NLinkPlugin plugin = NLinkPluginManager.getInstance().getPlugin(getFrom());
        String pluginId = plugin != null ? plugin.getId() : null;
        if (player == null) {
            ClientboundGenericAckPacket packet = new ClientboundGenericAckPacket(
                    "send_message_ack",
                    1,
                    "Player(Target) not found",
                    pluginId,
                    getFrom()
            );
            packet.sendTo(getFrom());
            return;
        }

        player.sendMessage(message);

        ClientboundGenericAckPacket packet = new ClientboundGenericAckPacket(
                "send_message_ack",
                0,
                "Message sent",
                pluginId,
                getFrom()
        );
        packet.sendTo(getFrom());
    }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packetId", getPacketId());
        map.put("message", message);
        return Utils.GSON.toJson(map);
    }

    @Override
    public String getPacketId() {
        return "send_message";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public String getTarget() {
        return targetUUID;
    }

    public String getMessage() {
        return message;
    }

    public static ServerboundSendMessagePacket fromJson(JsonObject json, WebSocket ws) {
        try {
            String targetUUID = json.get("target").getAsString();
            String message = json.get("message").getAsString();

            return new ServerboundSendMessagePacket(targetUUID, message, ws);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse packet: " + json, e);
        }
    }
}
