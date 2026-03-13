package ru.ivent.util;

import lombok.experimental.UtilityClass;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * @author Laughina
 */
@UtilityClass
public class FutureUtils {

    private static final Runnable NOOP = () -> {};

    public CompletableFuture<Void> asVoid(@NotNull CompletableFuture<?> cf) {
        return cf.thenRun(NOOP);
    }
}
