package io.github.nekosora.nlink.network.packet.toServer.plugin;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.plugin.NLinkPluginManager;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.plugin.NLinkPluginImpl;
import io.github.nekosora.nlink.utils.Utils;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;

public class ServerboundCreatePluginPacket extends NLinkNetworkPacket {
    private final String pluginId;
    private final String name;
    private final String version;
    private final String author;
    private final String description;

    public ServerboundCreatePluginPacket(String pluginId, String name, String version, String author, String description, WebSocket from) {
        super(from);
        this.pluginId = pluginId;
        this.name = name;
        this.version = version;
        this.author = author;
        this.description = description;
    }

    @Override
    public void handle() {
        // 检查是否已存在同名插件
        if (NLinkPluginManager.getInstance().getPlugin(pluginId) != null) {
            ClientboundGenericAckPacket ack = new ClientboundGenericAckPacket(
                    "create_plugin_ack", 1, "Plugin already exists", pluginId, getFrom()
            );
            ack.sendTo(getFrom());
            return;
        }
        // 创建插件
        NLinkPluginImpl plugin = new NLinkPluginImpl.Builder(getFrom(), pluginId)
                .name(name)
                .version(version)
                .author(author)
                .description(description)
                .build();
        NLinkPluginManager.getInstance().registerPlugin(plugin);
        NLinkPluginManager.getInstance().callPluginLoad(plugin);
        NLinkPluginManager.getInstance().callPluginEnable(plugin);
        ClientboundGenericAckPacket ack = new ClientboundGenericAckPacket(
                "create_plugin_ack", 0, "Plugin created and enabled", pluginId, getFrom()
        );
        ack.sendTo(getFrom());
    }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        map.put("plugin_id", pluginId);
        map.put("name", name);
        map.put("version", version);
        map.put("author", author);
        map.put("description", description);
        return Utils.GSON.toJson(map);
    }

    @Override
    public String getPacketId() {
        return "create_plugin";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public static ServerboundCreatePluginPacket fromJson(JsonObject json, WebSocket ws) {
        String pluginId = json.get("plugin_id").getAsString();
        String name = json.get("name").getAsString();
        String version = json.get("version").getAsString();
        String author = json.get("author").getAsString();
        String description = json.get("description").getAsString();
        return new ServerboundCreatePluginPacket(pluginId, name, version, author, description, ws);
    }
} 