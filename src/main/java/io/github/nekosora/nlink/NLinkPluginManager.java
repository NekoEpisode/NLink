package io.github.nekosora.nlink;

import io.github.nekosora.nlink.plugin.NLinkPlugin;
import org.java_websocket.WebSocket;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NLinkPluginManager {
    private static NLinkPluginManager instance;

    private final Map<String, NLinkPlugin> pluginMap; // pluginId to plugin instance

    public NLinkPluginManager() {
        this.pluginMap = new ConcurrentHashMap<>();
    }

    public void registerPlugin(NLinkPlugin plugin) {
        this.pluginMap.put(plugin.getId(), plugin);
    }

    public void unregisterPlugin(NLinkPlugin plugin) {
        if (plugin.isEnabled()) {
            callPluginDisable(plugin);
        }
        this.pluginMap.remove(plugin.getId());
    }

    /**
     * 调用插件加载方法
     * @param plugin 插件
     */
    public void callPluginLoad(NLinkPlugin plugin) {
        plugin.onLoad();
    }

    /**
     * 调用插件启用方法
     * @param plugin 插件
     */
    public void callPluginEnable(NLinkPlugin plugin) {
        plugin.onEnable();
    }

    /**
     * 调用插件禁用方法
     * @param plugin 插件
     */
    public void callPluginDisable(NLinkPlugin plugin) {
        plugin.onDisable();
    }

    /**
     * 获取插件Map (id -> plugin实例)
     * @return 插件map
     */
    public Map<String, NLinkPlugin> getPluginMap() {
        return pluginMap;
    }

    /**
     * 通过插件id获得插件实例
     * @param id 插件id
     * @return 插件
     */
    public @Nullable NLinkPlugin getPlugin(String id) {
        return pluginMap.get(id);
    }

    /**
     * 通过插件websocket获得插件实例
     * @param ws 插件websocket
     * @return 插件
     */
    public @Nullable NLinkPlugin getPlugin(WebSocket ws) {
        return pluginMap.values().stream()
                .filter(plugin -> plugin.getWebSocket().equals(ws))
                .findFirst()
                .orElse(null);
    }

    public static NLinkPluginManager getInstance() {
        if (instance == null) {
            instance = new NLinkPluginManager();
        }
        return instance;
    }
}
