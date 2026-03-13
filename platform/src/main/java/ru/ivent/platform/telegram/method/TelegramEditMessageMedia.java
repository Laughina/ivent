package ru.ivent.platform.telegram.method;

import ru.ivent.http.EmbeddableContent;
import ru.ivent.platform.telegram.TelegramClient;
import ru.ivent.platform.telegram.model.InputMedia;

/**
 * @author Laughina
 */
public final class TelegramEditMessageMedia extends TelegramEdit<TelegramEditMessageMedia> {

    public TelegramEditMessageMedia(TelegramClient client) {
        super(client, "editMessageMedia", true);
    }

    public TelegramEditMessageMedia media(
            String type,
            String filename,
            EmbeddableContent attachment
    ) {
        return media(type, filename, attachment, null);
    }

    public TelegramEditMessageMedia media(
            String type,
            String filename,
            EmbeddableContent attachment,
            String caption
    ) {
        params.set("media", new InputMedia(type, "attach://1", caption, "HTML"));
        params.setFile("1", filename, attachment);
        return this;
    }

    public TelegramEditMessageMedia mediaUrl(
            String type,
            String url,
            String caption
    ) {
        params.set("media", new InputMedia(type, url, caption, "HTML"));
        return this;
    }
}