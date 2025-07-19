package io.github.nekosora.nlink.commands;

import io.github.nekosora.nlink.commands.virtual.NLinkVirtualCommand;
import io.github.nekosora.nlink.commands.virtual.VirtualCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class NLinkCommand implements CommandExecutor {
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
                    commandSender.sendMessage(Component.text("未找到指定的虚拟命令").color(NamedTextColor.RED));
                    return true;
                }

                String[] argsForVirtualCommand = strings.length > 3 ?
                        Arrays.copyOfRange(strings, 3, strings.length) :
                        new String[0];

                return virtualCommand.execute(commandSender, argsForVirtualCommand);
            }

            default -> {
                commandSender.sendMessage(Component.text("未找到指定的子命令").color(NamedTextColor.RED));
                return true;
            }
        }
    }
}
