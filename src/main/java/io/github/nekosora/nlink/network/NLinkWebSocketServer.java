package io.github.nekosora.nlink.network;

import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import static io.github.nekosora.nlink.NLink.logger;
import io.github.nekosora.nlink.network.packet.StateManager;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.NLinkPluginManager;
import io.github.nekosora.nlink.plugin.NLinkPlugin;

public class NLinkWebSocketServer extends WebSocketServer {
    // 存储每个连接的challenge
    private final Map<WebSocket, String> challengeMap = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    
    public NLinkWebSocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.info("Client connected: " + webSocket.getRemoteSocketAddress());
        
        // 生成challenge并发送给客户端
        String challenge = generateChallenge();
        challengeMap.put(webSocket, challenge);
        
        // 发送challenge给客户端
        com.google.gson.JsonObject challengeResponse = new com.google.gson.JsonObject();
        challengeResponse.addProperty("packet_id", "challenge");
        challengeResponse.addProperty("challenge", challenge);
        webSocket.send(challengeResponse.toString());
        
        logger.info("Challenge sent to client: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        // 清理challenge
        challengeMap.remove(webSocket);
        
        // 自动卸载所有与该WebSocket关联的插件
        NLinkPluginManager manager = NLinkPluginManager.getInstance();
        java.util.List<NLinkPlugin> toRemove = new java.util.ArrayList<>();
        for (NLinkPlugin plugin : manager.getPluginMap().values()) {
            if (plugin.getWebSocket().equals(webSocket)) {
                toRemove.add(plugin);
            }
        }
        for (NLinkPlugin plugin : toRemove) {
            manager.callPluginDisable(plugin);
            manager.unregisterPlugin(plugin);
            plugin.onUnLoad();
            logger.info("Plugin [" + plugin.getId() + "] unloaded due to WebSocket disconnect: " + webSocket.getRemoteSocketAddress());
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (message.length() > 10000) {
            ClientboundGenericAckPacket ack = new ClientboundGenericAckPacket(
                "packet_error", 1, "Packet size exceeds limit", null, conn
            );
            conn.send(ack.toJson());
            conn.close();
            logger.warning("Client disconnected because packet size is too big (" + message.length() + "): " + conn.getRemoteSocketAddress());
            return;
        }

        try {
            String packetId;
            try {
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(message).getAsJsonObject();
                packetId = json.get("packet_id").getAsString();
            } catch (Exception e) {
                ClientboundGenericAckPacket ack = new ClientboundGenericAckPacket(
                    "packet_error", 1, "Invalid JSON format", null, conn
                );
                conn.send(ack.toJson());
                return;
            }
            
            // challenge包不需要验证状态
            if ("challenge".equals(packetId)) {
                // 客户端发送challenge响应，这是无效的，应该忽略
                return;
            }
            
            boolean isLoginPacket = "login".equals(packetId);
            boolean isPlay = StateManager.PLAY.isPlay(conn);
            if (!isLoginPacket && !isPlay) {
                ClientboundGenericAckPacket ack = new ClientboundGenericAckPacket(
                    "state_error", 1, "Not logged in or not in PLAY state", null, conn
                );
                conn.send(ack.toJson());
                return;
            }
            NLinkNetworkPacket packet = NetworkRegistry.parsePacket(message, conn);
            if (packet != null) {
                packet.handle();
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            ClientboundGenericAckPacket ack = new ClientboundGenericAckPacket(
                "packet_error", 1, "Invalid JSON format", null, conn
            );
            conn.send(ack.toJson());
        } catch (IllegalArgumentException e) {
            ClientboundGenericAckPacket ack = new ClientboundGenericAckPacket(
                "packet_error", 1, "Unsupported packet type", null, conn
            );
            conn.send(ack.toJson());
        } catch (Exception e) {
            ClientboundGenericAckPacket ack = new ClientboundGenericAckPacket(
                "packet_error", 1, "Server processing error", null, conn
            );
            conn.send(ack.toJson());
            logger.warning("Packet processing failed: " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    @Override
    public void onStart() {
        logger.info("NLink WebSocket Server started.");
    }
    
    /**
     * 生成随机challenge
     */
    private String generateChallenge() {
        byte[] challengeBytes = new byte[32];
        secureRandom.nextBytes(challengeBytes);
        return Base64.getEncoder().encodeToString(challengeBytes);
    }
    
    /**
     * 获取连接的challenge
     */
    public String getChallenge(WebSocket webSocket) {
        return challengeMap.get(webSocket);
    }
    
    /**
     * 清理challenge（一次性使用）
     */
    public void clearChallenge(WebSocket webSocket) {
        challengeMap.remove(webSocket);
    }
    
    /**
     * 验证challenge响应并清理（已废弃，使用新的密码+challenge机制）
     */
    @Deprecated
    public boolean verifyChallenge(WebSocket webSocket, String challengeResponse) {
        String originalChallenge = challengeMap.get(webSocket);
        if (originalChallenge == null) {
            return false;
        }
        
        // 验证challenge响应
        boolean isValid = originalChallenge.equals(challengeResponse);
        
        // 无论验证成功与否，都清理challenge（一次性使用）
        challengeMap.remove(webSocket);
        
        return isValid;
    }
}
