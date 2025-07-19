package io.github.nekosora.nlink.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.NLinkPacketParser;
import io.github.nekosora.nlink.network.packet.toServer.api.player.ServerboundQueryPlayerInformationWithPlayerName;
import io.github.nekosora.nlink.network.packet.toServer.command.ServerboundRegisterCommandPacket;
import io.github.nekosora.nlink.network.packet.toServer.ServerboundSendMessagePacket;
import io.github.nekosora.nlink.network.packet.toServer.api.entity.ServerboundTeleportEntityPacket;
import io.github.nekosora.nlink.network.packet.toServer.ServerboundBatchPacket;
import io.github.nekosora.nlink.network.packet.toServer.plugin.ServerboundCreatePluginPacket;
import io.github.nekosora.nlink.network.packet.toServer.plugin.ServerboundUnloadPluginPacket;
import io.github.nekosora.nlink.network.packet.toServer.login.ServerboundLoginPacket;
import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkRegistry {
    private static final Map<String, NLinkPacketParser<?>> PARSER_REGISTRY = new ConcurrentHashMap<>();

    static {
        registerPacket("login", ServerboundLoginPacket::fromJson);
        registerPacket("send_message", ServerboundSendMessagePacket::fromJson);
        registerPacket("register_command", ServerboundRegisterCommandPacket::fromJson);
        registerPacket("create_plugin", ServerboundCreatePluginPacket::fromJson);
        registerPacket("unload_plugin", ServerboundUnloadPluginPacket::fromJson);
        registerPacket("batch_packet", ServerboundBatchPacket::fromJson);
        registerPacket("teleport_entity", ServerboundTeleportEntityPacket::fromJson);
        registerPacket("query_player_information_with_player_name", ServerboundQueryPlayerInformationWithPlayerName::fromJson);

    }

    /** 注册数据包解析器 */
    public static <T extends NLinkNetworkPacket> void registerPacket(
            String packetId,
            NLinkPacketParser<T> parser
    ) {
        PARSER_REGISTRY.put(packetId, parser);
    }

    /** 从JSON字符串实例化数据包 */
    public static NLinkNetworkPacket parsePacket(String jsonStr, WebSocket from) {
        try {
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            String packetId = json.get("packet_id").getAsString();

            NLinkPacketParser<?> parser = PARSER_REGISTRY.get(packetId);
            if (parser == null) {
                throw new IllegalArgumentException("Unknown packet_id: " + packetId);
            }
            return parser.parse(json, from);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse packet: " + jsonStr, e);
        }
    }
}