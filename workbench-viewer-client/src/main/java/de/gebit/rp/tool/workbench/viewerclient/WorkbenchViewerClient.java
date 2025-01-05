package de.gebit.rp.tool.workbench.viewerclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleSession;
import de.gebit.rp.tool.workbench.viewercommon.ItemBadge;
import de.gebit.rp.tool.workbench.viewercommon.LogItem;
import de.gebit.rp.tool.workbench.viewercommon.RawContent;

public final class WorkbenchViewerClient {

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchViewerClient.class);

    private String host;
    private int port;
    private boolean closed;

    private BlockingQueue<HttpRequest> outbox = new LinkedBlockingQueue<>();

    private ObjectMapper mapper;

    private HttpClient httpClient;

    private Thread outboxThread;


    private CountDownLatch stoppedLatch = new CountDownLatch(1);

    private WorkbenchViewerClient(String aHost, int aPort) {
        this.host = Objects.requireNonNull(aHost);
        this.port = aPort;

        logger.info("Create DevConsoleClient with host {} and port {}", host, port);

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        httpClient = HttpClient.newBuilder().build();

        outboxThread = new Thread(() -> {
            while (true) {
                try {
                    HttpRequest request = outbox.take();

                    if (request.uri() == null) {
                        logger.info("Shutdown requested");
                        stoppedLatch.countDown();
                        return;
                    } else {
                        sendToService(request);
                    }
                } catch (InterruptedException e) {
                    logger.info("Outbox thread interrupted");
                } catch (Error e) {
                    logger.info("Outbox thread error", e);
                }
            }
        });
        outboxThread.start();
    }

    private void sendToService(HttpRequest request) {
        HttpResponse<String> response = null;
        try {
            response = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            logger.error("Error sending request to console service", e);
        } catch (InterruptedException e) {
            logger.error("Error sending request to console service", e);
        }
        if (response.statusCode() != 200) {
            logger.error("Error sending to dev console, status code={}", response.statusCode());
            logger.error(response.body());
        }
    }

    public void close() {
        logger.info("Shutting down console thread");
        closed = true;
        // send poison pill
        outbox.offer(new HttpRequest() {
            @Override
            public Optional<BodyPublisher> bodyPublisher() {
                return Optional.empty();
            }

            @Override
            public String method() {
                return "";
            }

            @Override
            public Optional<Duration> timeout() {
                return Optional.empty();
            }

            @Override
            public boolean expectContinue() {
                return false;
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public Optional<HttpClient.Version> version() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }
        });
        try {
            stoppedLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static WorkbenchViewerClient of(String aHost, int aPort) {
        return new WorkbenchViewerClient(aHost, aPort);
    }

    public void appendRawContent(ConsoleItem aConsoleItem, String label, String text) {
        LogItem logItem = LogItem.builder().clientId(aConsoleItem.getClientId())
                .sessionId(aConsoleItem.getSessionId())
                .rawContent(RawContent.of(label, text))
                .build();

        append(logItem);
    }

    public void appendBadge(ConsoleItem aConsoleItem, ItemBadge badge) {
        LogItem logItem = LogItem.builder().clientId(aConsoleItem.getClientId())
                .sessionId(aConsoleItem.getSessionId())
                .badge(Objects.requireNonNull(badge))
                .build();
        append(logItem);
    }

    public void append(LogItem logItem) {
        try {
            String json = mapper.writeValueAsString(Objects.requireNonNull(logItem));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://" + host + ":" + port + "/api/log"))
                    .headers("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            process(request);

        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(ConsoleItem anItem) {
        if (anItem.getClientId() == null) {
            anItem.setClientId(UUID.randomUUID().toString());
        }
        anItem.setVersion(anItem.getVersion() + 1);
        try {
            String json = mapper.writeValueAsString(Objects.requireNonNull(anItem));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://" + host + ":" + port + "/api/append"))
                    .headers("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            process(request);

        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void process(HttpRequest request) {
        if (closed) {
            sendToService(request);
        } else {
            outbox.offer(request);
        }
    }

    public String startSession(String name) {
        String id = UUID.randomUUID().toString();
        ConsoleSession session = ConsoleSession.builder().id(id).name(Objects.requireNonNull(name)).build();

        try {
            String json = mapper.writeValueAsString(Objects.requireNonNull(session));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://" + host + ":" + port + "/api/session/start"))
                    .headers("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            process(request);

        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return id;
    }

    public void stopSession(String sessionId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://" + host + ":" + port + "/api/session/" + Objects.requireNonNull(sessionId) + "/stop"))
                    .headers("Content-Type", "application/json;charset=UTF-8")
                    .GET()
                    .build();

            process(request);

        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }


}
