package io.github.nekosora.nlink.network.packet.toClient.command;

import io.github.nekosora.nlink.utils.Utils;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientboundCommandExecutedPacket extends NLinkNetworkPacket {
    private final String commandNameSpace;
    private final UUID request_id;
    private final CommandSender commandSender;
    private final String pluginId;
    private final List<String> args;

    public ClientboundCommandExecutedPacket(String commandNameSpace, UUID requestId, CommandSender commandSender, String pluginId, List<String> args, WebSocket from) {
        super(from);
        this.commandNameSpace = commandNameSpace;
        this.request_id = requestId;
        this.commandSender = commandSender;
        this.pluginId = pluginId;
        this.args = args;
    }

    @Override
    public void handle() { }

    @Override
    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("packet_id", getPacketId());
        map.put("command_namespace", commandNameSpace);
        Map<String, Object> commandSenderMap = new HashMap<>();
        commandSenderMap.put("name", commandSender.getName());
        commandSenderMap.put("uuid", (commandSender instanceof Player ? ((Player) commandSender).getUniqueId() : "console"));
        if (commandSender instanceof Player player) {
            commandSenderMap.put("is_player", true);
            commandSenderMap.put("x", player.getLocation().getX());
            commandSenderMap.put("y", player.getLocation().getY());
            commandSenderMap.put("z", player.getLocation().getZ());
            commandSenderMap.put("world", player.getWorld().getName());
            commandSenderMap.put("yaw", player.getLocation().getYaw());
            commandSenderMap.put("pitch", player.getLocation().getPitch());
            commandSenderMap.put("game_mode", player.getGameMode().name());
            commandSenderMap.put("is_op", player.isOp());
            commandSenderMap.put("is_flying", player.isFlying());
            commandSenderMap.put("is_sneaking", player.isSneaking());
            commandSenderMap.put("is_sprinting", player.isSprinting());
        } else {
            commandSenderMap.put("is_player", false);
        }
        map.put("command_sender", commandSenderMap);
        map.put("plugin_id", pluginId);
        map.put("args", args);
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

    public String getPluginId() {
        return pluginId;
    }

    public List<String> getArgs() {
        return args;
    }
}
