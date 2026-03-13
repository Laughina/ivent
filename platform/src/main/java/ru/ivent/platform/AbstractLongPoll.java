package ru.ivent.platform;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractLongPoll<T> {

    Logger logger;

    private static final long   MAX_BACKOFF    = 10000;
    private static final long   MIN_BACKOFF    = 100;
    private static final double BACKOFF_FACTOR = 3;

    @SuppressWarnings("BusyWait")
    public final void start(Consumer<T> updateHandler) throws Exception {
        onStart();

        long delayMs = MIN_BACKOFF;

        while (true) {
            try {
                poll(updateHandler);
                delayMs = MIN_BACKOFF;
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception exception) {
                logger.error("LongPoll receiving updates failure", exception);
                logger.info("Retrying connection attempt in {}s", delayMs / 1000.0);

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }

                delayMs = Math.min((long) (delayMs * BACKOFF_FACTOR), MAX_BACKOFF);
            }
        }
    }

    protected void onStart() throws Exception {
    }

    protected abstract void poll(Consumer<T> updateHandler) throws Exception;
}
