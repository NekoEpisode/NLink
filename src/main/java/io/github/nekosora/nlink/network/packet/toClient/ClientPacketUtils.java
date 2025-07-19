package io.github.nekosora.nlink.network.packet.toClient;

import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;

import static io.github.nekosora.nlink.NLink.logger;

public class ClientPacketUtils {
    public static void showHandleClientboundPacketWarning(NLinkNetworkPacket packet) {
        logger.warning("**Received Clientbound Packet**");
        logger.warning("This usually happened when client reversed packet flow direction or your server received Malformed packets.");
        logger.warning("Please contact client(plugin) developer or check your code.");
        logger.warning("**Packet Info**");
        logger.warning("Packet Class: " + packet.getClass().getSimpleName());
        logger.warning("Packet ID: " + packet.getPacketId());
        logger.warning("Packet Data: " + packet.toJson());
        logger.warning("From: " + packet.getFrom().getRemoteSocketAddress());
        logger.warning("**End of Packet Data**");
    }
}
