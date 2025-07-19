package io.github.nekosora.nlink.commands.virtual;

import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundCommandExecutedPacket;
import io.github.nekosora.nlink.plugin.NLinkPlugin;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualCommandManager {
    private static final VirtualCommandManager INSTANCE = new VirtualCommandManager();

    // Namespace format: "plugin:command" like "nlink:teleport"
    private final Map<String, NLinkVirtualCommand> commandMap = new ConcurrentHashMap<>();

    private VirtualCommandManager() {}

    public static VirtualCommandManager getInstance() {
        return INSTANCE;
    }

    /**
     * 注册虚拟命令（自动添加命名空间前缀）
     * @param plugin 注册插件的实例
     * @param commandName 命令名（不含斜杠）
     * @param permission 所需权限
     * @return 是否注册成功（重名时返回false）
     */
    public boolean registerCommand(@NotNull NLinkPlugin plugin,
                                   @NotNull String commandName,
                                   String permission) {
        String namespacedCmd = buildNamespacedKey(plugin, commandName);

        return commandMap.putIfAbsent(namespacedCmd,
                new NLinkVirtualCommand(commandName, permission, plugin) {
                    @Override
                    public boolean execute(CommandSender sender, String[] args) {
                        // Execute virtual command
                        UUID requestId = UUID.randomUUID();

                        ClientboundCommandExecutedPacket commandExecutedPacket = new ClientboundCommandExecutedPacket(
                                namespacedCmd,
                                requestId,
                                sender,
                                plugin.getId(),
                                null
                        );

                        getRegisteredFrom().sendPacket(commandExecutedPacket);
                        return true;
                    }
                }) == null; // When putIfAbsent success, return null
    }

    /**
     * 注销命令
     * @param plugin 插件实例
     * @param commandName 命令名
     */
    public void unregisterCommand(@NotNull NLinkPlugin plugin,
                                  @NotNull String commandName) {
        commandMap.remove(buildNamespacedKey(plugin, commandName));
    }

    /**
     * 查找命令（供命令调度器调用）
     */
    public NLinkVirtualCommand getCommand(String namespacedCommand) {
        return commandMap.get(namespacedCommand);
    }

    /**
     * 获取所有注册的虚拟命令
     */
    public List<NLinkVirtualCommand> getCommands() {
        return commandMap.values().stream().toList();
    }

    // build namespace key
    private String buildNamespacedKey(NLinkPlugin plugin, String cmd) {
        return plugin.getId().toLowerCase() + ":" + cmd.toLowerCase();
    }
}