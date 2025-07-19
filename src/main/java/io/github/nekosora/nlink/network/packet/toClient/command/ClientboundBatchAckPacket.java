package io.github.nekosora.nlink.network.packet.toClient.command;

import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import org.java_websocket.WebSocket;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ClientboundBatchAckPacket extends NLinkNetworkPacket {
    private final List<ClientboundGenericAckPacket> acks;

    public ClientboundBatchAckPacket(List<ClientboundGenericAckPacket> acks, WebSocket from) {
        super(from);
        this.acks = acks;
    }

    @Override
    public void handle() { }

    @Override
    public String toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("packet_id", getPacketId());
        JsonArray arr = new JsonArray();
        for (ClientboundGenericAckPacket ack : acks) {
            arr.add(io.github.nekosora.nlink.utils.Utils.GSON.fromJson(ack.toJson(), JsonObject.class));
        }
        obj.add("acks", arr);
        return obj.toString();
    }

    @Override
    public String getPacketId() {
        return "batch_ack";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public List<ClientboundGenericAckPacket> getAcks() {
        return acks;
    }
} 