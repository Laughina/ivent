package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;

/**
 * @author Laughina
 */
public final class TelegramDeleteMessage extends TelegramMethod<Boolean> {

    public TelegramDeleteMessage(TelegramClient client) {
        super(client, "deleteMessage", Boolean.class);
    }

    public TelegramDeleteMessage chatId(long chatId) {
        params.set("chat_id", chatId);
        return this;
    }

    public TelegramDeleteMessage messageId(long messageId) {
        params.set("message_id", messageId);
        return this;
    }
}
