package de.protubero.devconsole.wsmodel;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.protubero.devconsole.model.ConsoleItem;
import de.protubero.devconsole.model.SessionInfo;

@Service
public final class SocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    private static AtomicLong idGenerator = new AtomicLong();

    private LinkedList<SessionInfo> sessionList = new LinkedList<>();
    private LinkedList<ConsoleItem> consoleItemList = new LinkedList<>();

    private final List<WebSocketSession> webSocketSessions = new ArrayList<>();

    //private BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

    private ObjectMapper objectMapper;

    public SocketHandler(ObjectMapper anObjectMapper) {
        objectMapper = anObjectMapper;

        logger.info("+++++++++++++++++++++");
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {

        // we do not receive messages
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession session) throws Exception {
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


    private void sendToAllSessions(ClientMessage message) {
        webSocketSessions.stream().forEach(wss -> sendToOneSession(wss, message));
    }

    //
    // External API
    //
    //

    public void append(ConsoleItem clientItem)  {
        clientItem.setId(idGenerator.incrementAndGet());
        if (clientItem.getTimestamp() == null) {
            clientItem.setTimestamp(LocalDateTime.now());
        }

        consoleItemList.add(clientItem);

        sendToAllSessions(new ClientMessage("items", Collections.singletonList(clientItem)));
    }

    public void sessionInfo(SessionInfo sessionInfo) {
        if (sessionInfo.getProperties() == null) {
            sessionInfo.setProperties(new HashMap<>());
        }

        Optional<SessionInfo> existingSession = sessionList.stream().filter(si -> si.getSessionId().equals(sessionInfo.getSessionId()))
                .findFirst();
        if (existingSession.isPresent()) {
            existingSession.get().setName(sessionInfo.getName());
            existingSession.get().getProperties().putAll(sessionInfo.getProperties());
        } else {
            sessionList.add(sessionInfo);
        }

        sendToAllSessions(new ClientMessage("sessions", sessionList));
    }


}