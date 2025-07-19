package io.github.nekosora.nlink.network.packet;

import org.java_websocket.WebSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum StateManager {
    LOGIN,
    PLAY;

    public final Map<WebSocket, StateManager> stateMap = new ConcurrentHashMap<>();

    public void setState(WebSocket webSocket, StateManager state) {
        stateMap.put(webSocket, state);
    }

    public StateManager getState(WebSocket webSocket) {
        return stateMap.get(webSocket);
    }

    public void removeState(WebSocket webSocket) {
        stateMap.remove(webSocket);
    }

    public boolean isLogin(WebSocket webSocket) {
        return getState(webSocket) == LOGIN;
    }

    public boolean isPlay(WebSocket webSocket) {
        return getState(webSocket) == PLAY;
    }
}
