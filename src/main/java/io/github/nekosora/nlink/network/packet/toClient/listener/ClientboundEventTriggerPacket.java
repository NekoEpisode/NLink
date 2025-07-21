package io.github.nekosora.nlink.network.packet.toClient.listener;

import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import org.java_websocket.WebSocket;

public class ClientboundEventTriggerPacket extends NLinkNetworkPacket {
    public ClientboundEventTriggerPacket(WebSocket from) {
        super(from);
    }

    @Override
    public void handle() {

    }

    @Override
    public String toJson() {
        return "";
    }

    @Override
    public String getPacketId() {
        return "";
    }

    @Override
    public void sendTo(WebSocket webSocket) {

    }
}
