package io.github.nekosora.nlink.network;

import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

import static io.github.nekosora.nlink.NLink.logger;
import io.github.nekosora.nlink.network.packet.StateManager;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.NLinkPluginManager;
import io.github.nekosora.nlink.plugin.NLinkPlugin;

public class NLinkWebSocketServer extends WebSocketServer {
    public NLinkWebSocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.info("Client connected: " + webSocket.getRemoteSocketAddress());

    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
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
}
