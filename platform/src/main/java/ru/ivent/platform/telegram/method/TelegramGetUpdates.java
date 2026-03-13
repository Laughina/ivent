package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;
import ru.ivent.platform.telegram.model.Update;

import java.util.List;

/**
 * @author Laughina
 */
public final class TelegramGetUpdates extends TelegramMethod<Update[]> {

    public TelegramGetUpdates(TelegramClient client) {
        super(client, "getUpdates", Update[].class);
    }

    public TelegramGetUpdates offset(Integer offset) {
        params.set("offset", offset);
        return this;
    }

    public TelegramGetUpdates limit(Integer limit) {
        params.set("limit", limit);
        return this;
    }

    public TelegramGetUpdates timeout(Integer timeout) {
        params.set("timeout", timeout);
        return this;
    }

    public TelegramGetUpdates allowedUpdates(List<String> allowedUpdates) {
        params.set("allowedUpdates", allowedUpdates);
        return this;
    }

}
