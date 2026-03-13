package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;

/**
 * @author Laughina
 */
public final class TelegramEditMessageCaption extends TelegramEdit<TelegramEditMessageCaption> {

    public TelegramEditMessageCaption(TelegramClient client) {
        super(client, "editMessageCaption");
    }

    public TelegramEditMessageCaption caption(String caption) {
        params.set("caption", caption);
        return this;
    }

}
