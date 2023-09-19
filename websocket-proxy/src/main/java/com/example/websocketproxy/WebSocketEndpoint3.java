package com.example.websocketproxy;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/jupyter/{serverCode}/api/kernels/{id}/channels")
@Component
public class WebSocketEndpoint3 {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEndpoint3.class);

    private static final StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

    private WebSocketSession serverWsSession;

    @OnOpen
    public void onOpen(Session browserSession, @PathParam("serverCode") String serverCode, @PathParam("id") String id) throws Exception {
        logger.info("connected with browser. sessionId: {}", browserSession.getId());
        String path = browserSession.getRequestURI().getPath()
                .replace("{serverCode}", serverCode)
                .replace("{id}", id);

        Map<String, List<String>> requestParameterMap = browserSession.getRequestParameterMap();
        path += "?session_id=" + requestParameterMap.get("session_id").get(0);

        if (serverWsSession == null) {
            serverWsSession = webSocketClient.execute(new ServerWebSocketHandler(browserSession), "ws://211.42.179.151:50200" + path).get();
        }
    }

    @OnMessage
    public void onMessage(Session browserSession, String message) throws Exception {
        logger.info("from browser: sessionId: {}, message: {}", browserSession.getId(), message);
        serverWsSession.sendMessage(new TextMessage(message));
    }

    @OnClose
    public void onClose(Session browserSession) throws IOException {
        logger.info("browser ws session is closed.");
        if (serverWsSession != null) {
            serverWsSession.close();
        }
    }

    @OnError
    public void onError(Throwable t) throws IOException {
        logger.error("browser ws session has error. {}", t.getMessage());
        if (serverWsSession != null) {
            serverWsSession.close();
        }
    }
}

