package ru.ivent.platform.telegram;

import lombok.NoArgsConstructor;

/**
 * @author Laughina
 */
@NoArgsConstructor
public final class TelegramException extends RuntimeException {

    public TelegramException(String message) {
        super(message);
    }

    public TelegramException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public Throwable initCause(Throwable cause) {
        return this;
    }

}
