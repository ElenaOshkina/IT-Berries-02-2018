package com.itberries2018.demo.websockets;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itberries2018.demo.mechanics.events.game.Turn;
import com.itberries2018.demo.mechanics.events.logic.Move;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@SuppressWarnings("OverlyBroadThrowsClause")
public class GameWebSocketHandler extends TextWebSocketHandler {


    private static final Logger LOGGER = LoggerFactory.getLogger(GameWebSocketHandler.class);
    @NotNull
    private final RemotePointService service;

    @Autowired
    private final ObjectMapper objectMapper;

    public GameWebSocketHandler(@NotNull RemotePointService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable throwable) throws Exception {
        LOGGER.warn("Transportation problem", throwable);
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        LOGGER.info("Disconnected user with id  " + session.getAttributes().get("userId"));
        service.disconnectedHandler(Long.valueOf((session.getAttributes().get("userId").toString())));
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        service.registerUser(Long.valueOf(session.getAttributes().get("userId").toString()), session);
        LOGGER.info("Connected user with id  " + session.getAttributes().get("userId"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage jsonTextMessage) throws Exception {
        final Long userId = Long.valueOf(((Integer) session.getAttributes().get("userId")));
        try {
            final Message message = objectMapper.readValue(jsonTextMessage.getPayload(), Message.class);
            if (message.getClass() == Move.class || message.getClass() == Turn.class) {
                service.handleGameMessage(message, userId);
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            LOGGER.error("wrong json format response", ex);
        }
    }
}
