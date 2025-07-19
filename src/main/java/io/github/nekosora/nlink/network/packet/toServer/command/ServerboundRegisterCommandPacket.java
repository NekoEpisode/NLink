package io.github.nekosora.nlink.network.packet.toServer.command;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.NLink;
import io.github.nekosora.nlink.NLinkPluginManager;
import io.github.nekosora.nlink.utils.Utils;
import io.github.nekosora.nlink.commands.virtual.VirtualCommandManager;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundCommandExecutedPacket;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.plugin.NLinkPlugin;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.github.nekosora.nlink.NLink.logger;

public class ServerboundRegisterCommandPacket extends NLinkNetworkPacket {
    private final String commandName;
    private final String permission;
    private final boolean isReal;

    public ServerboundRegisterCommandPacket(String commandName, String permission, boolean isReal, WebSocket from) {
        super(from);
        this.commandName = commandName;
        this.permission = permission;
        this.isReal = isReal;
    }

    @Override
    public void handle() {
        // Register Command
        if (isReal) {
            NLinkPlugin plugin = NLinkPluginManager.getInstance().getPlugin(getFrom());
            String pluginId = plugin != null ? plugin.getId() : null;
            // Register real command (not /nlink commands xxxx)
            NLink.instance.getCommandManager().command(NLink.instance.getCommandManager().commandBuilder(commandName)
                    .handler(ctx -> {
                        UUID request_id = UUID.randomUUID();
                        ClientboundCommandExecutedPacket commandExecutedPacket = new ClientboundCommandExecutedPacket(
                                "real:" + commandName,
                                request_id,
                                ctx.sender(),
                                pluginId,
                                null // set null, because it's server send, no from.
                        );
                        commandExecutedPacket.sendTo(getFrom());
                    })
            );

            ClientboundGenericAckPacket responsePacket = new ClientboundGenericAckPacket(
                    "register_command_ack",
                    0,
                    "success",
                    pluginId,
                    null
            );
            responsePacket.addExtraData("command_name", commandName);
            responsePacket.sendTo(getFrom());
        } else {
            // Register virtual command (/nlink commands xxxx)
            NLinkPlugin plugin = NLinkPluginManager.getInstance().getPlugin(getFrom());
            String pluginId = plugin != null ? plugin.getId() : null;
            if (plugin != null)
                if (VirtualCommandManager.getInstance().registerCommand(plugin, commandName, permission)) {
                    ClientboundGenericAckPacket responsePacket = new ClientboundGenericAckPacket(
                            "register_command_ack",
                            0,
                            "success",
                            pluginId,
                            null
                    );
                    responsePacket.addExtraData("command_name", commandName);
                    responsePacket.sendTo(getFrom());
                    logger.info("Virtual command registered: " + commandName);
                } else {
                    ClientboundGenericAckPacket responsePacket = new ClientboundGenericAckPacket(
                            commandName,
                            1,
                            "command_name_conflict",
                            pluginId,
                            null
                    );
                    responsePacket.addExtraData("command_name", commandName);
                    responsePacket.sendTo(getFrom());
                    logger.warning("Virtual command register failed: " + commandName);
                }
            else
                logger.warning("RegisterCommandPacket was received, but there's no plugin was found!");
        }
    }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        map.put("command_name", commandName);
        map.put("permission", permission);
        map.put("is_real", isReal);
        return Utils.GSON.toJson(map);
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    @Override
    public String getPacketId() {
        return "register_command";
    }

    public String getCommandName() {
        return commandName;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isReal() {
        return isReal;
    }

    public static ServerboundRegisterCommandPacket fromJson(JsonObject json, WebSocket ws) {
        try {
            String commandName = json.get("command_name").getAsString();
            String permission = json.get("permission").getAsString();
            boolean isReal = json.get("is_real").getAsBoolean();

            return new ServerboundRegisterCommandPacket(commandName, permission, isReal, ws);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse packet: " + json, e);
        }
    }
}
