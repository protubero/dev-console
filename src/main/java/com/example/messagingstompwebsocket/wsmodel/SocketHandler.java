package com.example.messagingstompwebsocket.wsmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.messagingstompwebsocket.controller.SessionInfo;
import com.example.messagingstompwebsocket.wsmodel.ClientConsoleItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public final class SocketHandler extends TextWebSocketHandler {

    private LinkedList<ClientSession> sessionList = new LinkedList<>();
    private LinkedList<ClientConsoleItem> consoleItemList = new LinkedList<>();

    private final List<WebSocketSession> webSocketSessions = new ArrayList<>();

    private ObjectMapper objectMapper;

    public SocketHandler() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {

        // we do not receive messages
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketSessions.add(session);

        // send initial data to new client
        sendToOneSession(session, new ClientMessage("command", "reset"));
        sendToOneSession(session, new ClientMessage("sessions", sessionList));
        sendToOneSession(session, new ClientMessage("items", consoleItemList));

        webSocketSessions.add(session);
    }

    private void sendToOneSession(WebSocketSession session, ClientMessage message) {
        String jsonItem;
        try {
            jsonItem = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        TextMessage textMessage = new TextMessage(jsonItem);

        if (session.isOpen()) {
            try {
                session.sendMessage(textMessage);
            } catch (IllegalStateException exc) {
                // ignore
            } catch (IOException e) {
                // ignore
                //throw new RuntimeException(e);
            }
        }
    }

    public synchronized void append(ClientConsoleItem clientItem)  {
        sendToAllSessions(new ClientMessage("items", Collections.singletonList(clientItem)));
        consoleItemList.add(clientItem);
    }

    public synchronized void sessionInfo(SessionInfo sessionInfo) {
        String sessionId = Objects.requireNonNull(sessionInfo.getSessionId());
        ClientSession session = sessionMap.get(sessionId);
        if (session == null) {
            session = new ClientSession();
            session.setSessionId(sessionId);
            sessionMap.put(sessionId, session);
        }
        if (sessionInfo.getName() != null) {
            session.setName(sessionInfo.getName());
        }
        if (sessionInfo.getProperties() != null) {
            for (Map.Entry<String, String> entry : sessionInfo.getProperties().entrySet()) {
                session.setProperty(entry.getKey(), entry.getValue());
            }
        }

        sendToAllSessions(new ClientMessage("sessions", sessionList));
    }

    private void sendToAllSessions(ClientMessage message) {
        webSocketSessions.stream().forEach(wss -> sendToOneSession(wss, message));
    }

}