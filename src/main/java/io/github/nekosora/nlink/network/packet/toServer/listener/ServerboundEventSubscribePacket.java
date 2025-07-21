package io.github.nekosora.nlink.network.packet.toServer.listener;

import com.google.gson.JsonObject;
import io.github.nekosora.nlink.NLink;
import io.github.nekosora.nlink.network.packet.toClient.listener.ClientboundEventTriggerPacket;
import io.github.nekosora.nlink.plugin.NLinkPluginManager;
import io.github.nekosora.nlink.listener.NLinkListenerManager;
import io.github.nekosora.nlink.network.packet.NLinkNetworkPacket;
import io.github.nekosora.nlink.network.packet.toClient.command.ClientboundGenericAckPacket;
import io.github.nekosora.nlink.plugin.NLinkPlugin;
import io.github.nekosora.nlink.utils.CommandSenderUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.java_websocket.WebSocket;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.UUID;

import static io.github.nekosora.nlink.NLink.logger;

public class ServerboundEventSubscribePacket extends NLinkNetworkPacket {
    private final String eventClassPath;
    private final String listenerId;

    public ServerboundEventSubscribePacket(String eventClassPath, WebSocket from) {
        super(from);
        this.eventClassPath = eventClassPath;
        this.listenerId = "listener-" + UUID.randomUUID();
    }

    public ServerboundEventSubscribePacket(String eventClassPath, String listenerId, WebSocket from) {
        super(from);
        this.eventClassPath = eventClassPath;
        this.listenerId = listenerId;
    }

    @Override
    public void handle() {
        NLinkPlugin plugin = NLinkPluginManager.getInstance().getPlugin(getFrom());
        if (plugin == null) {
            logger.warning("Trying to register listener but plugin is null!");
            return;
        }

        try {
            Class<?> eventClassRaw = Class.forName(eventClassPath);

            if (!Event.class.isAssignableFrom(eventClassRaw)) {
                throw new IllegalArgumentException("Provided class is not a Bukkit event");
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) eventClassRaw;

            Listener listener = new Listener() {}; // 空接口实现

            PluginManager pm = Bukkit.getPluginManager();

            // 用EventExecutor统一处理所有事件
            pm.registerEvent(eventClass, listener, EventPriority.NORMAL, (l, event) -> {
                if (eventClass.isInstance(event)) {
                    JsonObject eventJson = serializeEventToJson(event);
                    new ClientboundEventTriggerPacket(eventJson, getFrom()).sendTo(getFrom());
                }
            }, NLink.instance);

            NLinkListenerManager.getInstance().registerListener(
                    listenerId,
                    plugin,
                    eventClassPath,
                    listener
            );

            ClientboundGenericAckPacket ackPacket = new ClientboundGenericAckPacket(
                    "event_subscribe_ack",
                    0,
                    "Listener registered",
                    plugin.getId(),
                    getFrom()
            );
            ackPacket.sendTo(getFrom());

            logger.info("[" + plugin.getId() + "] Registered listener " + listenerId + " for event " + eventClassPath);
        } catch (ClassNotFoundException e) {
            logger.severe("Failed to register listener " + listenerId + ": " + e.getMessage());
        }
    }

    /**
     * 将事件对象序列化为JSON
     */
    private JsonObject serializeEventToJson(Event event) {
        JsonObject json = new JsonObject();
        json.addProperty("event_class", event.getClass().getName());
        json.addProperty("event_name", event.getEventName());

        // 特殊处理getPlayer方法
        try {
            Method getPlayer = event.getClass().getMethod("getPlayer");
            if (CommandSender.class.isAssignableFrom(getPlayer.getReturnType())) {
                CommandSender sender = (CommandSender) getPlayer.invoke(event);
                json.add("player", CommandSenderUtils.convertCommandSenderOrPlayerToJson(sender));
            }
        } catch (Exception e) {
            // 没有getPlayer方法或调用失败，忽略
            logger.severe("出错了: " + e);
        }

        // 获取所有公共getter方法
        Arrays.stream(event.getClass().getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> (m.getName().startsWith("get") || m.getName().startsWith("is")))
                .filter(m -> !m.getName().equals("getClass"))
                .filter(m -> !m.getName().equals("getPlayer"))  // 跳过getPlayer，避免覆盖
                .forEach(method -> {
                    try {
                        String propertyName = getPropertyName(method);
                        Object value = method.invoke(event);
                        addPropertyToJson(json, propertyName, value);
                    } catch (Exception ignored) {}
                });

        return json;
    }

    private String getPropertyName(Method method) {
        String name = method.getName();
        if (name.startsWith("get")) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        } else if (name.startsWith("is")) {
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        }
        return name;
    }

    private void addPropertyToJson(JsonObject json, String name, Object value) {
        switch (value) {
            case null -> {
            }
            case Number number -> json.addProperty(name, number);
            case Boolean b -> json.addProperty(name, b);
            case String s -> json.addProperty(name, s);
            case Character c -> json.addProperty(name, c);
            default -> json.addProperty(name, value.toString());
        }
    }

    @Override
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("packet_id", getPacketId());
        json.addProperty("event_class_path", eventClassPath);
        json.addProperty("listener_id", listenerId);
        return json.toString();
    }

    @Override
    public String getPacketId() {
        return "event_subscribe";
    }

    @Override
    public void sendTo(WebSocket webSocket) {
        webSocket.send(toJson());
    }

    public String getEventClassPath() {
        return eventClassPath;
    }

    public String getListenerId() {
        return listenerId;
    }

    public static ServerboundEventSubscribePacket fromJson(JsonObject jsonObject, WebSocket from) {
        String eventClassPath = jsonObject.get("event_class_path").getAsString();
        if (jsonObject.has("listener_id")) {
            return new ServerboundEventSubscribePacket(
                    eventClassPath,
                    jsonObject.get("listener_id").getAsString(),
                    from
            );
        }
        return new ServerboundEventSubscribePacket(eventClassPath, from);
    }
}
