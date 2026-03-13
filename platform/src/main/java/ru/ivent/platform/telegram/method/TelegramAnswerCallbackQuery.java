package ru.ivent.platform.telegram.method;

import ru.ivent.platform.telegram.TelegramClient;

/**
 * @author Laughina
 */
public class TelegramAnswerCallbackQuery extends TelegramMethod<Boolean> {

    public TelegramAnswerCallbackQuery(TelegramClient client) {
        super(client, "answerCallbackQuery", Boolean.class);
    }

    public TelegramAnswerCallbackQuery queryId(String queryId) {
        params.set("callback_query_id", queryId);
        return this;
    }

    public TelegramAnswerCallbackQuery text(String text) {
            params.set("text", text);
            return this;
    }

    public TelegramAnswerCallbackQuery showAlert(boolean showAlert) {
            params.set("show_alert", showAlert);
            return this;
    }

    public TelegramAnswerCallbackQuery url(String url) {
        params.set("url", url);
        return this;
    }

    public TelegramAnswerCallbackQuery cacheTime(Integer cacheTime) {
        params.set("cache_time", cacheTime);
        return this;
    }
}
