package ru.ivent.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * @author Laughina
 */
public interface CommandManager {

    @NotNull CommandKeyboardButtonPayloadCodec getKeyboardButtonPayloadCodec();

    @Nullable Command getCommand(@NotNull String commandName);

    @Nullable CommandKeyboardButtonExecutor getKeyboardButtonExecutor(@NotNull String name);

    @NotNull CommandKeyboardButton registerKeyboardButton(
            @NotNull String name,
            @NotNull CommandKeyboardButtonExecutor executor
    );

    boolean register(@NotNull Command command);

    boolean unregister(@NotNull Command command);

    boolean unregister(@NotNull String commandName);

    @Unmodifiable @NotNull List<@NotNull Command> getCommands();
}
