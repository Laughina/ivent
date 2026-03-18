package ru.ivent.command;

import ru.ivent.model.IdentityHolder;
import ru.ivent.model.InKeyboardCallback;
import ru.ivent.model.OutMessage;
import ru.ivent.model.SentMessage;
import ru.ivent.platform.Platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class KeyboardContext implements ArgumentProvider {

    @Getter
    Platform platform;
    @Getter
    String name;
    Object[] arguments;
    @Getter
    InKeyboardCallback keyboardCallback;
    JsonMapper jsonMapper;

    public IdentityHolder chat() {
        return keyboardCallback.getChat();
    }

    public IdentityHolder from() {
        return keyboardCallback.getFrom();
    }

    @Override
    public String argument(int i) {
        return rawArgument(i)
                .map(Object::toString)
                .orElseThrow();
    }

    public @NotNull Optional<Object> rawArgument(int i) {
        return isValidIndex(i)
                ? Optional.ofNullable(arguments[i])
                : Optional.empty();
    }

    public <T> @NotNull Optional<T> argumentAs(int i, Class<T> type) {
        return rawArgument(i).flatMap(arg -> readValue(arg, type));
    }

    private <T> @NotNull Optional<T> readValue(@NotNull Object value, Class<T> type) {
        try {
            return Optional.ofNullable(jsonMapper.readValue(value.toString(), type));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    private boolean isValidIndex(int i) {
        return i >= 0 && i < arguments.length;
    }

    @Override
    public int argumentCount() {
        return arguments.length;
    }

    public CompletableFuture<SentMessage> sendMessage(OutMessage message) {
        return platform.sendMessage(message);
    }

    public CompletableFuture<Void> deleteMessage() {
        return platform.deleteMessage(keyboardCallback);
    }

    public CompletableFuture<Void> editMessage(OutMessage message) {
        return platform.editMessage(keyboardCallback, message);
    }

    public CompletableFuture<Void> answerCallback(@Nullable String text) {
        return platform.answerCallback(keyboardCallback, text);
    }
}
