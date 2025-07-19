package io.github.nekosora.nlink.commands;

import io.github.nekosora.nlink.NLinkPluginManager;
import io.github.nekosora.nlink.commands.virtual.NLinkVirtualCommand;
import io.github.nekosora.nlink.commands.virtual.VirtualCommandManager;
import io.github.nekosora.nlink.plugin.NLinkPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NLinkCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 0) {
            commandSender.sendMessage(
                    Component.text("NLink")
                            .color(NamedTextColor.AQUA)
                            .append(
                                    Component.text(" by NekoSora")
                                            .color(NamedTextColor.WHITE)
                            )
            );

            commandSender.sendMessage("/nlink commands <插件子命令> : 执行注册的任一非real命令");
            return true;
        }

        String subCommand = strings[0];
        switch (subCommand.toLowerCase()) {
            case "commands" -> {
                if (strings.length == 1) {
                    commandSender.sendMessage("当前已注册的所有虚拟命令:");
                    for (NLinkVirtualCommand virtualCommand : VirtualCommandManager.getInstance().getCommands()) {
                        commandSender.sendMessage(virtualCommand.getCommandName());
                    }
                    return true;
                }

                String virtualCommandName = strings[1];
                NLinkVirtualCommand virtualCommand = VirtualCommandManager.getInstance().getCommand(virtualCommandName);
                if (virtualCommand == null) {
                    // 尝试模糊查找（不带命名空间时）
                    if (!virtualCommandName.contains(":")) {
                        java.util.List<NLinkVirtualCommand> matches = VirtualCommandManager.getInstance().getCommands().stream()
                                .filter(cmd -> cmd.getCommandName().equalsIgnoreCase(virtualCommandName))
                                .toList();
                        if (matches.size() == 1) {
                            virtualCommand = matches.getFirst();
                        } else if (matches.size() > 1) {
                            commandSender.sendMessage(Component.text("有多个插件注册了该命令，请使用 <插件id>:<命令名> 格式指定").color(NamedTextColor.YELLOW));
                            return true;
                        }
                    }
                }
                if (virtualCommand == null) {
                    commandSender.sendMessage(Component.text("未找到指定的虚拟命令").color(NamedTextColor.RED));
                    return true;
                }

                String[] argsForVirtualCommand = strings.length > 2 ?
                        Arrays.copyOfRange(strings, 2, strings.length) :
                        new String[0];

                return virtualCommand.execute(commandSender, argsForVirtualCommand);
            }

            case "plugins" -> {
                commandSender.sendMessage("当前已注册的所有插件:");
                for (NLinkPlugin plugin : NLinkPluginManager.getInstance().getPluginMap().values()) {
                    commandSender.sendMessage(plugin.getName() + " - " + plugin.getVersion() + " [ID: " + plugin.getId() + "]");
                }
                return true;
            }

            default -> {
                commandSender.sendMessage(Component.text("未找到指定的子命令").color(NamedTextColor.RED));
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("commands", "plugins");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("commands")) {
            // 补全所有虚拟命令（命名空间和无命名空间）
            List<NLinkVirtualCommand> cmds = VirtualCommandManager.getInstance().getCommands();
            return cmds.stream()
                    .map(cmd -> {
                        String id = cmd.getRegisteredFrom().getId();
                        return id.toLowerCase() + ":" + cmd.getCommandName();
                    }).distinct().collect(Collectors.toList());
        }
        return List.of();
    }
}
