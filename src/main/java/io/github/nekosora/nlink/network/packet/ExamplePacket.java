package io.github.nekosora.nlink.network.packet;

import io.github.nekosora.nlink.utils.Utils;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;

public class ExamplePacket extends NLinkNetworkPacket {
    private String value1;
    private boolean value2;

    public ExamplePacket(String value1, boolean value2, WebSocket from) {
        super(from);
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public void handle() {
        System.out.println("ExamplePacket received: " + value1 + ", " + value2);
    }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        return Utils.GSON.toJson(map);
    }

    @Override
    public String getPacketId() {
        return "example";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }
}
