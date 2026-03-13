package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;
import ru.ivent.platform.telegram.model.File;

/**
 * @author Laughina
 */
public final class TelegramGetFile extends TelegramMethod<File> {

    public TelegramGetFile(TelegramClient client) {
        super(client, "getFile", File.class);
    }

    public TelegramGetFile fileId(String fileId) {
        params.set("file_id", fileId);
        return this;
    }
}
