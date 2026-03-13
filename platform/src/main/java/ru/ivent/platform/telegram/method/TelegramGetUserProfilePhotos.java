package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;
import ru.ivent.platform.telegram.model.UserProfilePhotos;

/**
 * @author Laughina
 */
public final class TelegramGetUserProfilePhotos extends TelegramMethod<UserProfilePhotos> {

    public TelegramGetUserProfilePhotos(TelegramClient client) {
        super(client, "getUserProfilePhotos", UserProfilePhotos.class);
    }

    public TelegramGetUserProfilePhotos userId(long userId) {
        params.set("user_id", userId);
        return this;
    }

    public TelegramGetUserProfilePhotos offset(int offset) {
        params.set("offset", offset);
        return this;
    }

    public TelegramGetUserProfilePhotos limit(int limit) {
        params.set("limit", limit);
        return this;
    }
}
