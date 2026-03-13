package ru.ivent.platform.telegram;

import ru.ivent.platform.AbstractLongPoll;
import ru.ivent.platform.telegram.model.Update;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import org.slf4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class TelegramLongPoll extends AbstractLongPoll<Update> {

    private static final int TIMEOUT = 90;

    TelegramClient telegramClient;

    public TelegramLongPoll(Logger logger, TelegramClient telegramClient) {
        super(logger);

        this.telegramClient = telegramClient;
    }

    @NonFinal
    volatile Integer offset;

    @Override
    protected void poll(Consumer<Update> updateHandler) throws InterruptedException, ExecutionException {
        var updates = telegramClient.getUpdates()
                .timeout(TIMEOUT)
                .offset(offset)
                .make()
                .get();

        logger.debug("Received {} updates", updates.length);

        for (var update : updates) {
            try {
                updateHandler.accept(update);
            } catch (Exception exception) {
                logger.error("Cannot handle update", exception);
            }

            offset = update.getUpdateId() + 1;
        }
    }
}
