package io.github.nekosora.nlink.network.packet.toClient.listener;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import org.java_websocket.WebSocket;

public class ClientboundEventTriggerPacket extends NLinkNetworkPacket {
    private final JsonObject eventJson;

    public ClientboundEventTriggerPacket(JsonObject eventJson, WebSocket from) {
        super(from);
        this.eventJson = eventJson;
    }

    @Override
    public void handle() {}

    @Override
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("packet_id", getPacketId());
        json.add("event_data", eventJson);
        return json.toString();
    }

    @Override
    public String getPacketId() {
        return "event_trigger";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }
}
