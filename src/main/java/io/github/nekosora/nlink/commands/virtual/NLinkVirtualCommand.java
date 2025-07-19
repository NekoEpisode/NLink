package io.github.nekosora.nlink.commands.virtual;

import io.github.nekosora.nlink.plugin.NLinkPlugin;
import org.bukkit.command.CommandSender;

public abstract class NLinkVirtualCommand {
    private final String commandName;
    private final String permission;
    private final NLinkPlugin registeredFrom;

    public NLinkVirtualCommand(String commandName, String permission, NLinkPlugin registeredFrom) {
        this.commandName = commandName;
        this.permission = permission;
        this.registeredFrom = registeredFrom;
    }

    public abstract boolean execute(CommandSender commandSender, String[] args);

    public String getCommandName() {
        return commandName;
    }

    public String getPermission() {
        return permission;
    }

    public NLinkPlugin getRegisteredFrom() {
        return registeredFrom;
    }
}
