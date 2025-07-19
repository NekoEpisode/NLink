package io.github.nekosora.nlink;

import io.github.nekosora.nlink.commands.NLinkCommand;
import io.github.nekosora.nlink.network.NLinkWebSocketServer;
import io.github.nekosora.nlink.utils.PasswordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.logging.Logger;

public final class NLink extends JavaPlugin {
    public static Logger logger;
    public static NLink instance;
    public static FileConfiguration config;

    private LegacyPaperCommandManager<CommandSender> commandManager;

    @Override
    public void onLoad() {
        logger = getLogger();
        instance = this;
        config = getConfig();

        saveDefaultConfig();

        NLinkWebSocketServer webSocketServer = new NLinkWebSocketServer(new InetSocketAddress(
                Objects.requireNonNull(getConfig().getString("webSocketAddress")),
                getConfig().getInt("webSocketPort")
        ));

        if (Objects.requireNonNull(getConfig().getString("password")).isEmpty()) {
            // 生成16位随机密码（字母+数字+符号）
            String password = generateSecurePassword(16);
            getConfig().set("password", PasswordUtils.sha256(password));
            saveConfig();
        }

        webSocketServer.start();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        commandManager = LegacyPaperCommandManager.createNative(
                this,
                ExecutionCoordinator.simpleCoordinator()
        );

        Objects.requireNonNull(getCommand("nlink")).setExecutor(new NLinkCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public LegacyPaperCommandManager<CommandSender> getCommandManager() {
        return commandManager;
    }

    private static String generateSecurePassword(int length) {
        final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        final String DIGITS = "0123456789";
        final String SYMBOLS = "!@#$%^&*_=+-/";

        String allChars = CHAR_LOWER + CHAR_UPPER + DIGITS + SYMBOLS;
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(allChars.length());
            sb.append(allChars.charAt(randomIndex));
        }
        return sb.toString();
    }
}
