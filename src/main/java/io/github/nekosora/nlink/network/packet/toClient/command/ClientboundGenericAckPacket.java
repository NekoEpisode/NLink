package io.github.nekosora.nlink.network.packet.toClient.command;

import io.github.nekosora.nlink.Utils;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.ClientPacketUtils;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;

public class ClientboundGenericAckPacket extends NLinkNetworkPacket {
    private final String requestType;  // like "register_command", "execute_command"
    private final int status;          // status code (0 = success, other = error code)
    private final String message;
    private final Map<String, Object> extraData;

    public ClientboundGenericAckPacket(String requestType, int status, String message, WebSocket from) {
        super(from);
        this.requestType = requestType;
        this.status = status;
        this.message = message;
        this.extraData = new HashMap<>();
    }

    // Add extra data
    public ClientboundGenericAckPacket addExtraData(String key, Object value) {
        this.extraData.put(key, value);
        return this;
    }

    @Override
    public void handle() { ClientPacketUtils.showHandleClientboundPacketWarning(this); }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        map.put("request_type", requestType);
        map.put("status", status);
        map.put("message", message);
        if (!extraData.isEmpty()) {
            map.put("data", extraData);
        }
        return Utils.GSON.toJson(map);
    }

    @Override
    public String getPacketId() {
        return "generic_ack";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    // Getters
    public String getRequestType() { return requestType; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, Object> getExtraData() { return extraData; }
}