package io.github.nekosora.nlink.network.packet;

import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;

public interface NLinkPacketParser<T extends NLinkNetworkPacket> {
    /** 从JSON解析出具体的数据包对象 */
    T parse(JsonObject json, WebSocket from);
}