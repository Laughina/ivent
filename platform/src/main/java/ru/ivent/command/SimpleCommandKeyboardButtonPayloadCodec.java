package ru.ivent.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class SimpleCommandKeyboardButtonPayloadCodec implements CommandKeyboardButtonPayloadCodec {

    JsonMapper jsonMapper;

    @Override
    public @NotNull String serialize(@NotNull CommandKeyboardButtonPayload payload) {
        try {
            return jsonMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public @NotNull Optional<@NotNull CommandKeyboardButtonPayload> deserialize(@NotNull String payload) {
        try {
            return Optional.of(jsonMapper.readValue(payload, CommandKeyboardButtonPayload.class));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }
}
