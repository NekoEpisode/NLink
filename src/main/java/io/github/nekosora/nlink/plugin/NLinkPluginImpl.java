package io.github.nekosora.nlink.plugin;

import io.github.nekosora.nlink.commands.virtual.VirtualCommandManager;
import io.github.nekosora.nlink.listener.NLinkListenerManager;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import org.java_websocket.WebSocket;

import static io.github.nekosora.nlink.NLink.logger;

public class NLinkPluginImpl implements NLinkPlugin {
    private final WebSocket ws;
    private final String id;
    private final String description;
    private final String name;
    private final String version;
    private final String author;

    private boolean enabled = false;
    private boolean loaded = false;

    // Only use Builder
    private NLinkPluginImpl(Builder builder) {
        this.ws = builder.ws;
        this.id = builder.id;
        this.description = builder.description;
        this.name = builder.name;
        this.version = builder.version;
        this.author = builder.author;
    }

    // Builder class
    public static class Builder {
        private final WebSocket ws;
        private final String id;
        private String description = "No description";
        private String name = "Unnamed Plugin";
        private String version = "1.0.0";
        private String author = "Unknown";

        public Builder(WebSocket ws, String id) {
            this.ws = ws;
            this.id = id;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public NLinkPluginImpl build() {
            return new NLinkPluginImpl(this);
        }
    }

    @Override
    public void onLoad() {
        loaded = true;
        logger.info("Enabling plugin " + id + " v" + version + " by " + author + "...");
    }

    @Override
    public void onUnLoad() {
        loaded = false;
    }

    @Override
    public void onEnable() {
        enabled = true;
        logger.info("Plugin " + id + " v" + version + " enabled");
    }

    @Override
    public void onDisable() {
        enabled = false;
        VirtualCommandManager.getInstance().unregisterAllCommand(this);
        NLinkListenerManager.getInstance().unregisterAllListeners(this);
        logger.info("Disabling plugin " + id + " v" + version + "...");
    }

    @Override
    public void sendPacket(NLinkNetworkPacket packet) {
        packet.sendTo(ws);
    }

    // Getter/Setter
    @Override
    public WebSocket getWebSocket() {
        return ws;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }
}