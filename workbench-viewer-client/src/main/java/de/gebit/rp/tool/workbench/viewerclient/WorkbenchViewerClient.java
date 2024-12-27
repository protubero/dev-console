package de.gebit.rp.tool.workbench.viewerclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.protubero.devconsole.common.ConsoleItem;
import de.protubero.devconsole.common.LogItem;

public final class WorkbenchViewerClient {

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchViewerClient.class);

    private String host;
    private int port;

    private BlockingQueue<HttpRequest> outbox = new LinkedBlockingQueue<>();

    private ObjectMapper mapper;

    private HttpClient httpClient;

    private Thread outboxThread;

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

                    HttpResponse<String> response = httpClient
                            .send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 200) {
                        logger.error("Error sending to dev console, status code={}", response.statusCode());
                        logger.error(response.body());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    logger.error("Error sending to dev console {}", e);
                }
            }
        });
        outboxThread.start();
    }

    public void close() {
        logger.info("Shutting down console thread");

        //TODO use poison pill to stop the thread
        // in order to ensure that all messages are sent which are in the pipeline
        // at this point in time
        outboxThread.interrupt();
    }

    public static WorkbenchViewerClient of(String aHost, int aPort) {
        return new WorkbenchViewerClient(aHost, aPort);
    }

    public void append(ConsoleItem aConsoleItem, String label, String text) {
        LogItem logItem = LogItem.builder().clientId(aConsoleItem.getClientId())
                .sessionId(aConsoleItem.getSessionId())
                .label(Objects.requireNonNull(label))
                .text(Objects.requireNonNull(text))
                .build();

        try {
            String json = mapper.writeValueAsString(Objects.requireNonNull(logItem));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://" + host + ":" + port + "/api/log"))
                    .headers("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            outbox.offer(request);

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

            outbox.offer(request);

        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }


}
