package com.example.websocketserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ProxyWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProxyWebSocketHandler.class);

    @Override
    protected void handleTextMessage(WebSocketSession proxySession, TextMessage message) throws Exception {
        logger.info("received message from proxy");
        proxySession.sendMessage(new TextMessage("this is server response. :" + message.getPayload()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession proxySession, CloseStatus status) throws Exception {
        logger.info("session with proxy server is closed. {}", status);
    }
}