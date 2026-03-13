package ru.ivent.platform.telegram.method;

import ru.ivent.http.EmbeddableContent;
import ru.ivent.platform.telegram.TelegramClient;

import lombok.SneakyThrows;

/**
 * @author Laughina
 */
public final class TelegramSendPhoto extends TelegramSend<TelegramSendPhoto> {

    public TelegramSendPhoto(TelegramClient client) {
        super(client, "sendPhoto", true);
    }

    public TelegramSendPhoto caption(String caption) {
        params.set("caption", caption);
        return this;
    }

    @SneakyThrows
    public TelegramSendPhoto photo(String filename, EmbeddableContent content) {
        params.setFile("photo", filename, content);
        return this;
    }
}
