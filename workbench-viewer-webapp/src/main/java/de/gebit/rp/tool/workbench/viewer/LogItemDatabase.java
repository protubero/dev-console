package de.gebit.rp.tool.workbench.viewer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.shared.Registration;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleSession;
import de.gebit.rp.tool.workbench.viewercommon.LogItem;
import de.gebit.rp.tool.workbench.viewercommon.RawContent;
import jakarta.validation.Valid;

@Service
public class LogItemDatabase {

    private static final Logger logger = LoggerFactory.getLogger(LogItemDatabase.class);

    private static AtomicLong idGenerator = new AtomicLong();


    private List<ConsoleItem> consoleItemList = new ArrayList<>();
    private List<ConsoleSession> sessionList = new ArrayList<>();

    private Map<String, LogItemDatabaseListener> listenerMap = new HashMap<>();


    private ConsoleSession currentSession;
    private Consumer<ConsoleSession> newSessionAddedListener;

    public LogItemDatabase() {
    }

    public List<ConsoleItem> getConsoleItemList() {
        return consoleItemList;
    }

    public void append(LogItem logItem) {
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
        if (logItem.getRawContent() != null) {
            var rawContentLog = logItem.getRawContent();
            if (existingContent == null) {
                currentItem.setRaw(List.of(RawContent.of(rawContentLog.getLabel(), rawContentLog.getValue())));
            } else {
                Optional<RawContent> exRcWithSameLabel = existingContent.stream()
                        .filter(rc -> rc.getLabel().equals(rawContentLog.getLabel())).findAny();
                if (exRcWithSameLabel.isPresent()) {
                    exRcWithSameLabel.get().setValue(exRcWithSameLabel.get().getValue() + rawContentLog.getValue());
                } else {
                    ArrayList<RawContent> nextList = new ArrayList<>(existingContent);
                    nextList.add(RawContent.of(rawContentLog.getLabel(), rawContentLog.getValue()));
                    currentItem.setRaw(List.of(nextList.toArray(new RawContent[nextList.size()])));
                }
            }
        }
        if (logItem.getBadge() != null) {
            logger.debug("add badge " + logItem.getBadge());
            if (currentItem.getBadges() == null) {
                currentItem.setBadges(new ArrayList<>());
            }
            currentItem.getBadges().add(logItem.getBadge());
        }
        if (logItem.getDuration() != null) {
            logger.debug("update duration " + logItem.getDuration());
            currentItem.setDuration(logItem.getDuration());
        }


        if (logger.isDebugEnabled()) {
            logger.debug(String.valueOf(logItem));
        }
        itemUpdated(currentItem);
    }

    public void append(ConsoleItem clientItem) {
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

                    itemUpdated(currentItem);
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

        if (logger.isDebugEnabled()) {
            logger.debug(String.valueOf(clientItem));
        }
        // create unknonw Session on the fly
        if (sessionById(clientItem.getSessionId()).isEmpty()) {
            ConsoleSession adHocSession = ConsoleSession.builder().id(clientItem.getSessionId()).name(clientItem.getSessionId()).build();
            sessionList.add( adHocSession);
            sessionAdded(adHocSession);
        }
        consoleItemList.add(clientItem);
        itemAdded(clientItem);
    }


    private void itemAdded(ConsoleItem item) {
        new ArrayList<>(listenerMap.values()).forEach(listener -> {
            listener.onNewItem(item);
        });
    }

    private void itemUpdated(ConsoleItem item) {
        new ArrayList<>(listenerMap.values()).forEach(listener -> {
            listener.onItemChanged(item);
        });
    }

    private void sessionAdded(ConsoleSession session) {
        new ArrayList<>(listenerMap.values()).forEach(listener -> {
            listener.onNewSessionAdded(session);
        });
    }

    public Registration addListener(LogItemDatabaseListener listener) {
        String uuid = UUID.randomUUID().toString();
        logger.info("Attach listener " + uuid);
        listenerMap.put(uuid, Objects.requireNonNull(listener));
        return new Registration() {
            @Override
            public void remove() {
                logger.info("Detach listener " + uuid);
                listenerMap.remove(uuid);
            }
        };
    }

    public void clear() {
        consoleItemList.clear();
    }

    public void startSession(@Valid ConsoleSession session) {
        Objects.requireNonNull(session);
        Objects.requireNonNull(session.getName());

        Optional<ConsoleSession> existingSession = sessionById(Objects.requireNonNull(session.getId()));
        if (existingSession.isPresent()) {
            existingSession.get().setName(session.getName());
            currentSession = existingSession.get();
        } else {
            sessionList.add(session);
            sessionAdded(session);
            currentSession = session;
        }
        currentSession.setStartTimestamp(LocalDateTime.now());

    }

    public void stopSession(@Valid String sessionId) {
        Optional<ConsoleSession> existingSession = sessionById(sessionId);
        if (existingSession.isEmpty()) {
            throw new RuntimeException("Invalid session id: " + sessionId);
        }
        existingSession.get().setStopTimestamp(LocalDateTime.now());
    }

    public List<ConsoleSession> getConsoleSessionList() {
        return sessionList;
    }

    public Optional<ConsoleSession> sessionById(String sessionId) {
        Objects.requireNonNull(sessionId);
        return sessionList.stream().filter(cs -> cs.getId().equals(sessionId)).findAny();
    }

}
