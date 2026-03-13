package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;

/**
 * @author Laughina
 */
public final class TelegramSendMessage extends TelegramSend<TelegramSendMessage> {

    public TelegramSendMessage(TelegramClient client) {
        super(client, "sendMessage");
    }

    public TelegramSendMessage text(String text) {
        params.set("text", text);
        return this;
    }

    public TelegramSendMessage disableWebPageView(Boolean disableWebPageView) {
        params.set("disable_web_page_view", disableWebPageView);
        return this;
    }
}
