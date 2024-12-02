package de.protubero.devconsole.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.protubero.devconsole.common.ConsoleItem;

public final class DevConsoleClient {

    private static final Logger logger = LoggerFactory.getLogger(DevConsoleClient.class);

    private String host;
    private int port;

    private ObjectMapper mapper;

    private HttpClient httpClient;

    private DevConsoleClient(String aHost, int aPort) {
        this.host = Objects.requireNonNull(aHost);
        this.port = aPort;

        logger.info("Create DevConsoleClient with host {} and port {}", host, port);

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        httpClient = HttpClient.newBuilder().build();
    }

    public static DevConsoleClient of(String aHost, int aPort) {
        return new DevConsoleClient(aHost, aPort);
    }

    public void send(ConsoleItem anItem) {
        try {
            String json = mapper.writeValueAsString(Objects.requireNonNull(anItem));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://" + host + ":" + port + "/api/append"))
                    .headers("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            CompletableFuture<HttpResponse<String>> response = HttpClient.newBuilder()
                    .build()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString());
            response.thenAccept(res -> {
                if (res.statusCode() != 200) {
                    logger.error("Error reaching dev console, status code={}", res.statusCode());
                    logger.error(res.body());
                }
            });

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
