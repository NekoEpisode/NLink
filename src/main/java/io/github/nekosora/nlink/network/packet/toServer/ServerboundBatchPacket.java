package io.github.nekosora.nlink.network.packet.toServer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.nekosora.nlink.network.NetworkRegistry;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.utils.Utils;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerboundBatchPacket extends NLinkNetworkPacket {
    private final List<JsonObject> packets;

    public ServerboundBatchPacket(List<JsonObject> packets, WebSocket from) {
        super(from);
        this.packets = packets;
    }

    @Override
    public void handle() {
        for (JsonObject packetJson : packets) {
            NLinkNetworkPacket packet = NetworkRegistry.parsePacket(packetJson.toString(), getFrom());
            if (packet != null) {
                packet.handle();
            }
        }
    }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        map.put("packets", packets);
        return Utils.GSON.toJson(map);
    }

    @Override
    public String getPacketId() {
        return "batch_packet";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public static ServerboundBatchPacket fromJson(JsonObject json, WebSocket from) {
        List<JsonObject> packets = new ArrayList<>();
        if (json.has("packets") && json.get("packets").isJsonArray()) {
            JsonArray arr = json.getAsJsonArray("packets");
            for (int i = 0; i < arr.size(); i++) {
                packets.add(arr.get(i).getAsJsonObject());
            }
        }
        return new ServerboundBatchPacket(packets, from);
    }
} 