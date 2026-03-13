package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;
import ru.ivent.platform.telegram.model.LinkPreviewOptions;
import ru.ivent.platform.telegram.model.Message;
import ru.ivent.platform.telegram.model.ReplyMarkup;

/**
 * @author Laughina
 */
public abstract class TelegramEdit<S extends TelegramEdit<S>> extends TelegramMethod<Message> {

    @SuppressWarnings("unchecked")
    protected final S self() {
        return (S) this;
    }

    protected TelegramEdit(TelegramClient client, String name) {
        super(client, name, Message.class);
    }

    public TelegramEdit(TelegramClient client, String name, boolean multipart) {
        super(client, name, Message.class, multipart);
    }

    public S chatId(long chatId) {
        params.set("chat_id", chatId);
        return self();
    }

    public S parseMode(String parseMode) {
        params.set("parse_mode", parseMode);
        return self();
    }

    public S messageId(long messageId) {
        params.set("message_id", messageId);
        return self();
    }

    public S replyMarkup(ReplyMarkup replyMarkup) {
        params.set("reply_markup", replyMarkup);
        return self();
    }

    public S linkPreviewOptions(LinkPreviewOptions linkPreviewOptions) {
        params.set("link_preview_options", linkPreviewOptions);
        return self();
    }
}