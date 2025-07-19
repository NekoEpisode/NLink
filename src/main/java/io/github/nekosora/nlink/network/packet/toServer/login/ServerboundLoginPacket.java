package io.github.nekosora.nlink.network.packet.toServer.login;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.NLink;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.utils.Utils;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;

public class ServerboundLoginPacket extends NLinkNetworkPacket {
    private final String hashedPassword; // sha-256后的密码

    public ServerboundLoginPacket(String hashedPassword, WebSocket from) {
        super(from);
        this.hashedPassword = hashedPassword;
    }

    public static ServerboundLoginPacket fromJson(JsonObject json, WebSocket ws) {
        String hashedPassword = json.get("hashedPassword").getAsString();
        return new ServerboundLoginPacket(hashedPassword, ws);
    }

    @Override
    public void handle() {
        String configPassword = NLink.config.getString("password");

        if (configPassword == null || configPassword.isEmpty()) {
            getFrom().send("{\"error\":\"Server password not configured\"}");
            return;
        }

        if (hashedPassword.equals(configPassword)) {
            getFrom().send("{\"status\":\"success\"}");
            // 登录成功后切换状态
            io.github.nekosora.nlink.network.packet.StateManager.PLAY.setState(getFrom(), io.github.nekosora.nlink.network.packet.StateManager.PLAY);
        } else {
            getFrom().send("{\"error\":\"Invalid password\"}");
        }
    }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        map.put("hashedPassword", hashedPassword);
        return Utils.GSON.toJson(map);
    }

    @Override
    public String getPacketId() {
        return "login";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
}
