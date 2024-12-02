package de.protubero.devconsole.server;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.protubero.devconsole.common.ConsoleItem;
import de.protubero.devconsole.common.SessionInfo;

@Service
public final class SocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    private static AtomicLong idGenerator = new AtomicLong();

    private List<SessionInfo> sessionList = new ArrayList<>();
    private List<ConsoleItem> consoleItemList = new ArrayList<>();

    private final List<WebSocketSession> webSocketSessions = new ArrayList<>();

    //private BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

    private ObjectMapper objectMapper;

    public SocketHandler(ObjectMapper anObjectMapper) {
        objectMapper = anObjectMapper;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {

        // we do not receive messages
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession session) {
        // send initial data to new client
        sendToOneSession(session, new ClientMessage("command", "reset"));
        sendToOneSession(session, new ClientMessage("sessions", sessionList.toArray(new SessionInfo[sessionList.size()])));
        sendToOneSession(session, new ClientMessage("items", consoleItemList.toArray(new ConsoleItem[consoleItemList.size()])));

        webSocketSessions.add(session);
    }

    private void sendToOneSession(WebSocketSession session, ClientMessage clientMessage) {
        sendToOneSession(session, asTextMessage(clientMessage));
    }

    private synchronized void sendToOneSession(WebSocketSession session, TextMessage message) {
        if (session.isOpen()) {
            try {
                session.sendMessage(message);
            } catch (IllegalStateException exc) {
                // ignore
            } catch (IOException e) {
                // ignore
                //throw new RuntimeException(e);
            }
        }
    }

    private TextMessage asTextMessage(Object obj) {
        String jsonItem;
        try {
            jsonItem = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new TextMessage(jsonItem);
    }

    private void sendToAllSessions(ClientMessage message) {
        TextMessage textMessage = asTextMessage(message);
        webSocketSessions.stream().forEach(wss -> sendToOneSession(wss, textMessage));
    }

    //
    // External API
    //
    //

    public synchronized void append(ConsoleItem clientItem)  {
        clientItem.setId(idGenerator.incrementAndGet());
        if (clientItem.getTimestamp() == null) {
            clientItem.setTimestamp(LocalDateTime.now());
        }
        if (clientItem.getRaw() != null && clientItem.getRaw().size() == 0) {
            clientItem.setRaw(null);
        }

        consoleItemList.add(clientItem);
        sendToAllSessions(new ClientMessage("items", new ConsoleItem[]{ clientItem }));
    }

    public synchronized void sessionInfo(SessionInfo sessionInfo) {
        if (sessionInfo.getProperties() == null) {
            sessionInfo.setProperties(new HashMap<>());
        }

        Optional<SessionInfo> existingSession = sessionList.stream().filter(si -> si.getSessionId().equals(sessionInfo.getSessionId()))
                .findFirst();
        if (existingSession.isPresent()) {
            existingSession.get().setName(sessionInfo.getName());
            existingSession.get().getProperties().putAll(sessionInfo.getProperties());
        } else {
            synchronized (sessionInfo) {
                sessionList.add(sessionInfo);
            }
        }

        sendToAllSessions(new ClientMessage("sessions", sessionList.toArray(new SessionInfo[sessionList.size()])));
    }


}