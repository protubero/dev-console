package de.gebit.rp.tool.workbench.viewer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vaadin.flow.shared.Registration;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;
import de.gebit.rp.tool.workbench.viewercommon.LogItem;
import de.gebit.rp.tool.workbench.viewercommon.RawContent;

@Service
public class LogItemDatabase {

    private static final Logger logger = LoggerFactory.getLogger(LogItemDatabase.class);

    private static AtomicLong idGenerator = new AtomicLong();


    private List<ConsoleItem> consoleItemList = new ArrayList<>();

    private Map<String, LogItemDatabaseListener> listenerMap = new HashMap<>();

    public LogItemDatabase() {
        /*
        String sessionId = "xyz";
        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                ConsoleItem newItem = ConsoleItem.builder()
                        .name("Text " + i)
                        .clientId(UUID.randomUUID().toString())
                        .sessionId(sessionId)
                        .raw(List.of(RawContent.of("label", "")))
                        .build();
                append(newItem);

                try {
                    Thread.sleep(500l);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }}).start();

        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                ArrayList<ConsoleItem> clonedList = null;
                synchronized (consoleItemList) {
                    clonedList = new ArrayList<>(consoleItemList);
                }
                clonedList.forEach(item -> {
                    LogItem newItem = LogItem.builder()
                            .clientId(item.getClientId())
                            .sessionId(item.getSessionId())
                            .label("label")
                            .text(".")
                            .build();
                    append(newItem);
                });

                try {
                    Thread.sleep(2000l);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }}).start();
        */
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
        switch (logItem.getType()) {
            case raw -> {
                var rawContentLog = logItem.getRawContentLog();
                if (existingContent == null) {
                    currentItem.setRaw(List.of(RawContent.of(rawContentLog.getLabel(), rawContentLog.getText())));
                } else {
                    Optional<RawContent> exRcWithSameLabel = existingContent.stream()
                            .filter(rc -> rc.getLabel().equals(rawContentLog.getLabel())).findAny();
                    if (exRcWithSameLabel.isPresent()) {
                        exRcWithSameLabel.get().setValue(exRcWithSameLabel.get().getValue() + rawContentLog.getText());
                    } else {
                        ArrayList<RawContent> nextList = new ArrayList<>(existingContent);
                        nextList.add(RawContent.of(rawContentLog.getLabel(), rawContentLog.getText()));
                        currentItem.setRaw(List.of(nextList.toArray(new RawContent[nextList.size()])));
                    }
                }
            }
            case badge -> {
                logger.debug("add badge " + logItem.getBadge());
                if (currentItem.getBadges() == null) {
                    currentItem.setBadges(new ArrayList<>());
                }
                currentItem.getBadges().add(logItem.getBadge());
            }
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
}
