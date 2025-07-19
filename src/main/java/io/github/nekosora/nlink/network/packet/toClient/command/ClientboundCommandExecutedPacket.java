package io.github.nekosora.nlink.network.packet.toClient.command;

import io.github.nekosora.nlink.utils.Utils;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.ClientPacketUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientboundCommandExecutedPacket extends NLinkNetworkPacket {
    private final String commandNameSpace;
    private final UUID request_id;
    private final CommandSender commandSender;
    private final String pluginId;

    public ClientboundCommandExecutedPacket(String commandNameSpace, UUID requestId, CommandSender commandSender, String pluginId, WebSocket from) {
        super(from);
        this.commandNameSpace = commandNameSpace;
        this.request_id = requestId;
        this.commandSender = commandSender;
        this.pluginId = pluginId;
    }

    @Override
    public void handle() { ClientPacketUtils.showHandleClientboundPacketWarning(this); }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        map.put("command_namespace", commandNameSpace);
        map.put("request_id", request_id);
        Map<String, Object> commandSenderMap = new HashMap<>();
        commandSenderMap.put("name", commandSender.getName());
        commandSenderMap.put("uuid", (commandSender instanceof Player ? ((Player) commandSender).getUniqueId() : "console"));
        map.put("command_sender", commandSenderMap);
        map.put("plugin_id", pluginId);
        return Utils.GSON.toJson(map);
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    @Override
    public String getPacketId() {
        return "command_executed";
    }

    public String getCommandNameSpace() {
        return commandNameSpace;
    }

    public UUID getRequest_id() {
        return request_id;
    }

    public CommandSender getCommandSender() {
        return commandSender;
    }
    public String getPluginId() { return pluginId; }
}
