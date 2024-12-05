package de.protubero.devconsole.server;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.protubero.devconsole.common.ConsoleItem;
import de.protubero.devconsole.common.LogItem;
import de.protubero.devconsole.common.RawContent;
import jakarta.validation.Valid;

@Service
public final class SocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);


    private class DevConsoleWebSession {
        private WebSocketSession session;
        private BlockingQueue<TextMessage> outbox = new LinkedBlockingQueue<>();
        private Thread thread;

        public DevConsoleWebSession(WebSocketSession aSession) {
            this.session = aSession;
        }

        public void start() {
            thread = new Thread(() -> {
                try {
                    TextMessage msg;
                    boolean sessionIsStillOpen = true;
                    do {
                        msg = outbox.take();

                        if (session.isOpen()) {
                            try {
                                session.sendMessage(msg);
                            } catch (IllegalStateException ise) {
                                // ignore
                                logger.error("Error sending msg to client", ise);
                            } catch (IOException e) {
                                // ignore
                                logger.error("Error sending msg to client", e);
                                //throw new RuntimeException(e);
                            }
                        } else {
                            logger.info("Web Socket already closed");
                        }

                    } while (session.isOpen());
                } catch (InterruptedException exc) {
                    logger.info("Session Thread interrupted");
                }
                logger.info("Close Web Socket Output Thread");
            });

            thread.start();
        }

        public void send(TextMessage message) {
            if (thread.isAlive()) {
                outbox.offer(message);
            }
        }
    }

    private static AtomicLong idGenerator = new AtomicLong();

    private List<ConsoleItem> consoleItemList = new ArrayList<>();

    private final List<DevConsoleWebSession> devConsoleSessions = new ArrayList<>();


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
    public synchronized void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Web Socket closed {}", status);
    }

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession session) {
        logger.info("Web Socket connected {}", session);

        DevConsoleWebSession devConsoleWebSession = new DevConsoleWebSession(session);
        devConsoleSessions.add(devConsoleWebSession);
        devConsoleWebSession.start();

        // send initial data to new client
        devConsoleWebSession.send(asTextMessage(new ClientMessage("reset", "")));

        // Attention! By copying the list into an array we also make sure to have an immutable snapshot
        // of the item list, which is required!
        devConsoleWebSession.send(asTextMessage(new ClientMessage("items", consoleItemList.toArray(new ConsoleItem[consoleItemList.size()]))));

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
        devConsoleSessions.stream().forEach(wss -> wss.send(textMessage));
    }

    //
    // External API
    //
    //
    public synchronized void log(@Valid LogItem logItem) {
        Optional<ConsoleItem> existingItem = consoleItemList.stream().filter(item -> item.getClientId().equals(logItem.getClientId()))
                .findAny();
        if (existingItem.isEmpty()) {
            throw new RuntimeException();
        }
        ConsoleItem currentItem = existingItem.get();
        if (!currentItem.getSessionId().equals(logItem.getSessionId())) {
            throw new RuntimeException("Different session ids");
        }
        List<RawContent> existingContent = currentItem.getRaw();
        if (existingContent == null) {
            currentItem.setRaw(List.of(RawContent.of(logItem.getLabel(), logItem.getText())));
        } else {
            Optional<RawContent> exRcWithSameLabel = existingContent.stream()
                    .filter(rc -> rc.getLabel().equals(logItem.getLabel())).findAny();
            if (exRcWithSameLabel.isPresent()) {
                exRcWithSameLabel.get().setValue(exRcWithSameLabel.get().getValue() + logItem.getText());
            } else {
                ArrayList<RawContent> nextList = new ArrayList<>(existingContent);
                nextList.add(RawContent.of(logItem.getLabel(), logItem.getText()));
                currentItem.setRaw(List.of(nextList.toArray(new RawContent[nextList.size()])));
            }
        }

        logger.info(String.valueOf(logItem));
        sendToAllSessions(new ClientMessage("update", currentItem));
    }


    public synchronized void append(ConsoleItem clientItem) {
        if (clientItem.getVersion() > 1) {
            if (clientItem.getClientId() == null) {
                throw new RuntimeException("Missing client id");
            }
            Optional<ConsoleItem> existingItem = consoleItemList.stream().filter(item -> item.getClientId().equals(clientItem.getClientId()))
                    .findAny();
            if (existingItem.isPresent()) {
                ConsoleItem currentItem = existingItem.get();
                if (clientItem.getVersion() != currentItem.getVersion() + 1) {
                    logger.error("Discard message");
                    throw new RuntimeException("");
                } else {
                    if (!currentItem.getSessionId().equals(clientItem.getSessionId())) {
                        throw new RuntimeException("Different session ids");
                    }

                    // update existing item with values from newer version
                    currentItem.setName(clientItem.getName());
                    if (clientItem.getRaw() != null && clientItem.getRaw().size() > 0) {
                        List<RawContent> existingContent = currentItem.getRaw();
                        if (existingContent == null) {
                            existingContent = List.of();
                            currentItem.setRaw(existingContent);
                        }
                        for (RawContent newContent : clientItem.getRaw()) {
                            Optional<RawContent> exRcWithSameLabel = existingContent.stream()
                                    .filter(rc -> rc.getLabel().equals(newContent.getLabel())).findAny();
                            if (exRcWithSameLabel.isPresent()) {
                                exRcWithSameLabel.get().setValue(newContent.getValue());
                            } else {
                                existingContent.add(newContent);
                            }
                        }
                    }
                    currentItem.setType(clientItem.getType());
                    currentItem.setHtmlText(clientItem.getHtmlText());
                    currentItem.setVersion(clientItem.getVersion());
                    if (clientItem.getTimestamp() != null) {
                        currentItem.setTimestamp(clientItem.getTimestamp());
                    }

                    sendToAllSessions(new ClientMessage("update", currentItem));
                }
                return;
            }
        }

        clientItem.setId(idGenerator.incrementAndGet());
        if (clientItem.getTimestamp() == null) {
            clientItem.setTimestamp(LocalDateTime.now());
        }
        if (clientItem.getRaw() != null && clientItem.getRaw().size() == 0) {
            clientItem.setRaw(null);
        }

        consoleItemList.add(clientItem);
        sendToAllSessions(new ClientMessage("items", new ConsoleItem[]{clientItem}));
    }


    /*
    public synchronized void update(@Valid ConsoleItem item) {
        for (int i = 0; i < ) {

        }
    }
    */

}