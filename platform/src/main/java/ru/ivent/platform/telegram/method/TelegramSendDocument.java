package ru.ivent.platform.telegram.method;

import ru.ivent.http.EmbeddableContent;
import ru.ivent.platform.telegram.TelegramClient;

import lombok.SneakyThrows;

/**
 * @author Laughina
 */
public final class TelegramSendDocument extends TelegramSend<TelegramSendDocument> {

    public TelegramSendDocument(TelegramClient client) {
        super(client, "sendDocument", true);
    }

    public TelegramSendDocument caption(String caption) {
        params.set("caption", caption);
        return this;
    }

    @SneakyThrows
    public TelegramSendDocument document(String filename, EmbeddableContent content) {
        params.setFile("document", filename, content);
        return this;
    }
}
