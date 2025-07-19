package io.github.nekosora.nlink.network.packet;

public interface NLinkNetworkPacket {
    void handle();
    void write();
    void read();
    byte[] getBytes();
    String toJson();
    String getPacketId();
}
