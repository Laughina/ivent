package ru.ivent.command;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Laughina
 */
public interface CommandKeyboardButtonPayloadCodec {

    @NotNull String serialize(@NotNull CommandKeyboardButtonPayload payload);

    @NotNull Optional<@NotNull CommandKeyboardButtonPayload> deserialize(@NotNull String payload);
}
