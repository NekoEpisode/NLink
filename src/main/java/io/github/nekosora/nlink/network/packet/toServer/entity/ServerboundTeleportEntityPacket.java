package io.github.nekosora.nlink.network.packet.toServer.entity;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.NLink;
import io.github.nekosora.nlink.plugin.NLinkPluginManager;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.plugin.NLinkPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;

import java.util.UUID;

public class ServerboundTeleportEntityPacket extends NLinkNetworkPacket {
    // 支持多种传送目标
    private final String entity; // 需要被传送的实体/玩家 UUID 或名称
    private final TargetLocation targetLocation; // 目标坐标，可为null
    private final String targetEntity; // 目标实体/玩家 UUID 或名称，可为null

    public static class TargetLocation {
        public final double x, y, z;
        public final float yaw, pitch;
        public TargetLocation(double x, double y, double z, float yaw, float pitch) {
            this.x = x; this.y = y; this.z = z; this.yaw = yaw; this.pitch = pitch;
        }
    }

    public ServerboundTeleportEntityPacket(String entity, TargetLocation targetLocation, String targetEntity, WebSocket from) {
        super(from);
        this.entity = entity;
        this.targetLocation = targetLocation;
        this.targetEntity = targetEntity;
    }

    @Override
    public void handle() {
        Bukkit.getScheduler().runTask(NLink.instance, () -> {
            Entity entityObj = resolveEntity(entity);
            if (entityObj == null) {
                sendAck(1, "Entity to teleport not found");
                return;
            }
            Location dest = null;
            if (targetEntity != null && !targetEntity.isEmpty()) {
                Entity target = resolveEntity(targetEntity);
                if (target != null) {
                    dest = target.getLocation();
                } else {
                    sendAck(1, "Target entity not found");
                    return;
                }
            } else if (targetLocation != null) {
                dest = new Location(entityObj.getWorld(), targetLocation.x, targetLocation.y, targetLocation.z, targetLocation.yaw, targetLocation.pitch);
            }
            if (dest == null) {
                sendAck(1, "No valid target location or entity");
                return;
            }
            entityObj.teleportAsync(dest).thenRun(() -> sendAck(0, "Teleport success")).exceptionally(ex -> {
                sendAck(1, "Teleport failed: " + ex.getMessage());
                return null;
            });
        });
    }

    private Entity resolveEntity(String idOrName) {
        if (idOrName == null) return null;
        if (idOrName.matches("[0-9a-fA-F\\-]{36}")) {
            try { return Bukkit.getEntity(UUID.fromString(idOrName)); } catch (Exception ignored) {}
        }
        Player player = Bukkit.getPlayerExact(idOrName);
        if (player != null) return player;
        return null;
    }

    private void sendAck(int status, String msg) {
        NLinkPlugin plugin = NLinkPluginManager.getInstance().getPlugin(getFrom());
        String pluginId = plugin != null ? plugin.getId() : null;
        ClientboundGenericAckPacket ackPacket = new ClientboundGenericAckPacket("teleport_entity_ack", status, msg, pluginId, getFrom());
        ackPacket.sendTo(getFrom());
    }

    @Override
    public String toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("packet_id", getPacketId());
        obj.addProperty("entity", entity);
        if (targetLocation != null) {
            JsonObject loc = new JsonObject();
            loc.addProperty("x", targetLocation.x);
            loc.addProperty("y", targetLocation.y);
            loc.addProperty("z", targetLocation.z);
            loc.addProperty("yaw", targetLocation.yaw);
            loc.addProperty("pitch", targetLocation.pitch);
            obj.add("target_location", loc);
        }
        if (targetEntity != null) obj.addProperty("target_entity", targetEntity);
        return obj.toString();
    }

    @Override
    public String getPacketId() {
        return "teleport_entity";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public String getEntity() { return entity; }
    public TargetLocation getTargetLocation() { return targetLocation; }
    public String getTargetEntity() { return targetEntity; }

    public static ServerboundTeleportEntityPacket fromJson(JsonObject jsonObject, WebSocket from) {
        String entity = jsonObject.get("entity").getAsString();
        TargetLocation targetLocation = null;
        String targetEntity = null;
        if (jsonObject.has("target_location")) {
            JsonObject loc = jsonObject.getAsJsonObject("target_location");
            targetLocation = new TargetLocation(
                loc.get("x").getAsDouble(),
                loc.get("y").getAsDouble(),
                loc.get("z").getAsDouble(),
                loc.get("yaw").getAsFloat(),
                loc.get("pitch").getAsFloat()
            );
        }
        if (jsonObject.has("target_entity")) {
            targetEntity = jsonObject.get("target_entity").getAsString();
        }
        return new ServerboundTeleportEntityPacket(entity, targetLocation, targetEntity, from);
    }
}
