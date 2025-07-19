package io.github.nekosora.nlink.network.packet.toServer.api.entity;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.NLink;
import io.github.nekosora.nlink.NLinkPluginManager;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.plugin.NLinkPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.java_websocket.WebSocket;

import java.util.UUID;

import static io.github.nekosora.nlink.NLink.logger;

public class ServerboundTeleportEntityPacket extends NLinkNetworkPacket {
    private final UUID entityUuid;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public ServerboundTeleportEntityPacket(UUID entityUuid, double x, double y, double z, float yaw, float pitch, WebSocket from) {
        super(from);
        this.entityUuid = entityUuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void handle() {
        Bukkit.getScheduler().runTask(NLink.instance, () -> {
            Entity entity = Bukkit.getEntity(entityUuid);
            if (entity == null) {
                NLinkPlugin plugin = NLinkPluginManager.getInstance().getPlugin(getFrom());
                if (plugin == null) {
                    logger.warning("Plugin not found on teleport_entity_ack");
                    return;
                }
                ClientboundGenericAckPacket ackPacket = new ClientboundGenericAckPacket("teleport_entity_ack", 1, "Entity not found", plugin.getId(), getFrom());
                ackPacket.sendTo(getFrom());
                return;
            }
            entity.teleportAsync(new Location(entity.getWorld(), x, y, z, yaw, pitch));
        });
    }

    @Override
    public String toJson() {
        return "";
    }

    @Override
    public String getPacketId() {
        return "teleport_entity";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public static ServerboundTeleportEntityPacket fromJson(JsonObject jsonObject, WebSocket from) {
        UUID entityUuid = UUID.fromString(jsonObject.get("entity_uuid").getAsString());
        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        double z = jsonObject.get("z").getAsDouble();
        float yaw = jsonObject.get("yaw").getAsFloat();
        float pitch = jsonObject.get("pitch").getAsFloat();
        return new ServerboundTeleportEntityPacket(entityUuid, x, y, z, yaw, pitch, from);
    }
}
