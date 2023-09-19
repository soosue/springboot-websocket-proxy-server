package com.example.websocketproxy;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;

    @ServerEndpoint("/jupyter/{serverCode}/api/events/subscribe")
@Component
public class WebSocketEndpoint2 {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEndpoint2.class);

    private static final StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

    private WebSocketSession serverWsSession;

    @OnOpen
    public void onOpen(Session browserSession, @PathParam("serverCode") String serverCode) throws Exception {
        logger.info("connected with browser. sessionId: {}", browserSession.getId());
        String path = browserSession.getRequestURI().getPath().replace("{serverCode}", serverCode);
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

