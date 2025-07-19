package io.github.nekosora.nlink.plugin;

import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import org.java_websocket.WebSocket;

public interface NLinkPlugin {
    void onLoad();
    void onUnLoad();
    void onEnable();
    void onDisable();
    String getId();
    String getDescription();
    String getName();
    String getVersion();
    String getAuthor();
    WebSocket getWebSocket();
    boolean isEnabled();
    boolean isLoaded();
    void sendPacket(NLinkNetworkPacket packet);
}
