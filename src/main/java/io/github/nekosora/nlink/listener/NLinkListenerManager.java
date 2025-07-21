package io.github.nekosora.nlink.listener;

import io.github.nekosora.nlink.plugin.NLinkPlugin;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NLinkListenerManager {
    private static final NLinkListenerManager instance = new NLinkListenerManager();

    // 核心数据结构
    private final Map<String, RegisteredListener> listenerRegistry = new ConcurrentHashMap<>();
    private final Map<NLinkPlugin, Map<String, List<String>>> pluginListeners = new ConcurrentHashMap<>();

    // 注册的监听器信息
    public record RegisteredListener(
            String listenerId,
            NLinkPlugin plugin,
            String classpath,
            Listener listener
    ) {}

    public static NLinkListenerManager getInstance() {
        return instance;
    }

    /**
     * 注册监听器（需提供listenerId）
     * @param listenerId 由调用方提供的唯一标识符
     * @return 是否注册成功（如果listenerId已存在则返回false）
     */
    public boolean registerListener(
            @NotNull String listenerId,
            @NotNull NLinkPlugin plugin,
            @NotNull String classpath,
            @NotNull Listener listener
    ) {
        // 检查listenerId是否已存在
        if (listenerRegistry.containsKey(listenerId)) {
            return false;
        }

        RegisteredListener registered = new RegisteredListener(
                listenerId, plugin, classpath, listener
        );

        // 全局注册
        listenerRegistry.put(listenerId, registered);

        // 插件级索引
        pluginListeners.computeIfAbsent(plugin, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(classpath, k -> new CopyOnWriteArrayList<>())
                .add(listenerId);

        return true;
    }

    /**
     * 批量注册监听器
     * @param listenerIds 与listeners一一对应的ID列表
     * @return 注册成功的数量
     */
    public int registerListeners(
            @NotNull List<String> listenerIds,
            @NotNull NLinkPlugin plugin,
            @NotNull String classpath,
            @NotNull Listener... listeners
    ) {
        if (listenerIds.size() != listeners.length) {
            throw new IllegalArgumentException("listenerIds和listeners长度必须一致");
        }

        int successCount = 0;
        for (int i = 0; i < listeners.length; i++) {
            if (registerListener(listenerIds.get(i), plugin, classpath, listeners[i])) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 通过listenerId获取监听器实例
     */
    public @Nullable Listener getListenerById(@NotNull String listenerId) {
        return Optional.ofNullable(listenerRegistry.get(listenerId))
                .map(RegisteredListener::listener)
                .orElse(null);
    }

    /**
     * 通过listenerId获取注册信息
     */
    public @Nullable RegisteredListener getListenerInfo(@NotNull String listenerId) {
        return listenerRegistry.get(listenerId);
    }

    /**
     * 通过listenerId移除监听器
     */
    public boolean unregisterListener(@NotNull String listenerId) {
        return Optional.ofNullable(listenerRegistry.remove(listenerId))
                .map(info -> {
                    // 从插件索引中移除
                    Optional.ofNullable(pluginListeners.get(info.plugin()))
                            .ifPresent(classpathMap -> {
                                classpathMap.get(info.classpath()).remove(listenerId);

                                // 清理空集合
                                if (classpathMap.get(info.classpath()).isEmpty()) {
                                    classpathMap.remove(info.classpath());
                                    if (classpathMap.isEmpty()) {
                                        pluginListeners.remove(info.plugin());
                                    }
                                }
                            });
                    return true;
                })
                .orElse(false);
    }

    /**
     * 移除插件的所有监听器
     */
    public void unregisterAllListeners(@NotNull NLinkPlugin plugin) {
        Optional.ofNullable(pluginListeners.remove(plugin))
                .ifPresent(classpathMap ->
                        classpathMap.values().forEach(listenerIds ->
                                listenerIds.forEach(listenerRegistry::remove)
                        )
                );
    }

    /**
     * 获取插件下特定事件类型的所有listenerId
     */
    public @Nullable List<String> getListenerIds(
            @NotNull NLinkPlugin plugin,
            @NotNull String classpath
    ) {
        return Optional.ofNullable(pluginListeners.get(plugin))
                .map(map -> map.get(classpath))
                .map(Collections::unmodifiableList)
                .orElse(null);
    }

    /**
     * 获取插件的所有监听器映射（listenerId按事件类型分组）
     */
    public @Nullable Map<String, List<String>> getPluginListenerIds(@NotNull NLinkPlugin plugin) {
        return Optional.ofNullable(pluginListeners.get(plugin))
                .map(Map::copyOf)
                .orElse(null);
    }

    /**
     * 检查listenerId是否已被使用
     */
    public boolean isListenerIdUsed(@NotNull String listenerId) {
        return listenerRegistry.containsKey(listenerId);
    }
}