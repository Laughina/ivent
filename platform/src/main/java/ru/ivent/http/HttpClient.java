package ru.ivent.http;

import java.util.concurrent.CompletableFuture;

/**
 * @author Laughina
 */
public interface HttpClient {

    void start();

    void stop();

    CompletableFuture<HttpResponse> post(String url, Content content);

    CompletableFuture<HttpResponse> get(String url);
}
