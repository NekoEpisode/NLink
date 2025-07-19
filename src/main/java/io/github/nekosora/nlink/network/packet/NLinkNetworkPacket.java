package io.github.nekosora.nlink.network.packet;

import org.java_websocket.WebSocket;

public abstract class NLinkNetworkPacket {
    private final WebSocket from;

    public NLinkNetworkPacket(WebSocket from) {
        this.from = from;
    }

    public abstract void handle();
    public abstract String toJson();
    public abstract String getPacketId();
    public abstract void sendTo(WebSocket webSocket);

    public WebSocket getFrom() {
        return from;
    }
}
