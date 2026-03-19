package ru.ivent.service.api.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * @author Laughina
 */
@Slf4j
public class JsonHttpClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    public static final Gson GSON = new GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public <T> T get(String url, Class<T> typeClass) throws IOException, InterruptedException {
        logger.debug("GET {}", url);

        HttpRequest request = httpRequest(url);
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }

        logger.debug("Response {} ({} chars)", response.statusCode(), response.body().length());
        return GSON.fromJson(response.body(), typeClass);
    }

    public String getRaw(String url) throws IOException, InterruptedException {
        HttpRequest request = httpRequest(url);
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }
        return response.body();
    }

    private HttpRequest httpRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("User-Agent", "ivent-parser/1.0")
                .GET()
                .build();
    }
}