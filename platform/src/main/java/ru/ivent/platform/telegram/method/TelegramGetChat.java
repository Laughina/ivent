package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;
import ru.ivent.platform.telegram.model.ChatFullInfo;

/**
 * @author Laughina
 */
public final class TelegramGetChat extends TelegramMethod<ChatFullInfo> {

    public TelegramGetChat(TelegramClient client) {
        super(client, "getChat", ChatFullInfo.class);
    }

    public TelegramGetChat setChatId(String chatId) {
        params.set("chat_id", chatId);
        return this;
    }

    public TelegramGetChat setChatId(Long chatId) {
        params.set("chat_id", chatId);
        return this;
    }
}
