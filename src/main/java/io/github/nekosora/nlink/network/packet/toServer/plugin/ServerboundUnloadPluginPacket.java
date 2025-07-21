package io.github.nekosora.nlink.network.packet.toServer.plugin;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.plugin.NLinkPluginManager;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.plugin.NLinkPlugin;
import io.github.nekosora.nlink.utils.Utils;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;

public class ServerboundUnloadPluginPacket extends NLinkNetworkPacket {
    private final String pluginId; // 可为null

    public ServerboundUnloadPluginPacket(String pluginId, WebSocket from) {
        super(from);
        this.pluginId = pluginId;
    }

    @Override
    public void handle() {
        NLinkPluginManager manager = NLinkPluginManager.getInstance();
        NLinkPlugin plugin;
        if (pluginId == null || pluginId.isEmpty()) {
            plugin = manager.getPlugin(getFrom());
        } else {
            plugin = manager.getPlugin(pluginId);
        }
        if (plugin == null) {
            ClientboundGenericAckPacket ack = new ClientboundGenericAckPacket(
                    "unload_plugin_ack", 1, "Plugin not found", pluginId, getFrom()
            );
            ack.sendTo(getFrom());
            return;
        }
        manager.callPluginDisable(plugin);
        manager.unregisterPlugin(plugin);
        plugin.onUnLoad();
        ClientboundGenericAckPacket ack = new ClientboundGenericAckPacket(
                "unload_plugin_ack", 0, "Plugin unloaded", plugin.getId(), getFrom()
        );
        ack.sendTo(getFrom());
    }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        map.put("plugin_id", pluginId);
        return Utils.GSON.toJson(map);
    }

    @Override
    public String getPacketId() {
        return "unload_plugin";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public static ServerboundUnloadPluginPacket fromJson(JsonObject json, WebSocket ws) {
        String pluginId = json.has("plugin_id") ? json.get("plugin_id").getAsString() : null;
        return new ServerboundUnloadPluginPacket(pluginId, ws);
    }
} 