package ru.ivent.command;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class CommandKeyboardButton {

    CommandKeyboardButtonPayloadCodec codec;

    @Getter
    String name;

    public @NotNull String asPayload(Object... arguments) {
        return codec.serialize(new CommandKeyboardButtonPayload(name, arguments));
    }
}
