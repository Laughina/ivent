package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;
import ru.ivent.platform.telegram.model.User;

/**
 * @author Laughina
 */
public final class TelegramGetMe extends TelegramMethod<User> {

    public TelegramGetMe(TelegramClient client) {
        super(client, "getMe", User.class);
    }

}
