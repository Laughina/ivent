package ru.ivent.platform;

import java.util.concurrent.CompletableFuture;

/**
 * @author Laughina
 */
public interface ApiMethod<R> {

    String name();

    Class<? extends R> type();

    CompletableFuture<R> make();

    ApiMethodParams params();
}
