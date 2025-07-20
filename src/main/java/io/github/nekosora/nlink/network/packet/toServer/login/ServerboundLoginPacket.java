package io.github.nekosora.nlink.network.packet.toServer.login;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.NLink;
import io.github.nekosora.nlink.network.NLinkWebSocketServer;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.utils.PasswordUtils;
import io.github.nekosora.nlink.utils.Utils;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ServerboundLoginPacket extends NLinkNetworkPacket {
    private final String hashedPassword; // sha256(password + challenge)的结果

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
        // 验证challenge响应
        NLinkWebSocketServer server = NLink.getWebSocketServer();
        if (server == null) {
            getFrom().send("{\"status\":\"error\",\"error\":\"Server instance not found\"}");
            return;
        }
        
        // 获取原始challenge
        String originalChallenge = server.getChallenge(getFrom());
        if (originalChallenge == null) {
            getFrom().send("{\"status\":\"error\",\"error\":\"No challenge found\"}");
            return;
        }

        String configPassword = NLink.config.getString("password");

        if (configPassword == null || configPassword.isEmpty()) {
            getFrom().send("{\"status\":\"error\",\"error\":\"Server password not configured\"}");
            return;
        }

        // 服务端计算: sha256(password + challenge)
        String expectedHash = PasswordUtils.sha256(configPassword + originalChallenge);
        
        // 验证客户端发送的hash
        if (hashedPassword.equals(expectedHash)) {
            getFrom().send("{\"status\":\"success\"}");
            // 登录成功后切换状态
            io.github.nekosora.nlink.network.packet.StateManager.PLAY.setState(getFrom(), io.github.nekosora.nlink.network.packet.StateManager.PLAY);
            // 清理challenge（一次性使用）
            server.clearChallenge(getFrom());
        } else {
            getFrom().send("{\"status\":\"error\",\"error\":\"Invalid password or challenge\"}");
            // 清理challenge（一次性使用）
            server.clearChallenge(getFrom());
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
