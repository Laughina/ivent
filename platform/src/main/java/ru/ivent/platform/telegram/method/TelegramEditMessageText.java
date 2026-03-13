package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;

/**
 * @author Laughina
 */
public final class TelegramEditMessageText extends TelegramEdit<TelegramEditMessageText> {

    public TelegramEditMessageText(TelegramClient client) {
        super(client, "editMessageText");
    }

    public TelegramEditMessageText text(String text) {
        params.set("text", text);
        return this;
    }
}
