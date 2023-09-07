package com.example.websocketproxy;

import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ServerWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerWebSocketHandler.class);
    private final Session browserSession;

    public ServerWebSocketHandler(Session browserSession) {
        this.browserSession = browserSession;
    }

    @Override
    protected void handleTextMessage(WebSocketSession serverSession, TextMessage message) throws Exception {
        browserSession.getBasicRemote().sendText(message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession serverSession, CloseStatus status) throws Exception {
        logger.info("session with server is closed");
    }
}