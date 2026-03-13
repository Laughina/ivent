package ru.ivent.platform.telegram.method;

import ru.ivent.platform.ApiMethod;
import ru.ivent.platform.telegram.TelegramClient;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.CompletableFuture;

/**
 * @author Laughina
 */
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class TelegramMethod<R> implements ApiMethod<R> {

    TelegramClient client;

    @Getter
    String name;

    @Getter
    Class<? extends R> type;

    @Getter
    TelegramMethodParams params;

    public TelegramMethod(TelegramClient client, String name, Class<? extends R> type) {
        this(client, name, type, new TelegramJsonMethodParams(client.getJsonMapper()));
    }

    public TelegramMethod(TelegramClient client, String name, Class<? extends R> type, boolean multipart) {
        this(client, name, type, !multipart
                ? new TelegramJsonMethodParams(client.getJsonMapper())
                : new TelegramMultipartMethodParams(client.getJsonMapper()));
    }

    @Override
    public CompletableFuture<R> make() {
        return client.send(this);
    }
}
