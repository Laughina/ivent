package ru.ivent.command;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class SimpleCommandManager implements CommandManager {

    @Getter
    CommandKeyboardButtonPayloadCodec keyboardButtonPayloadCodec;

    Set<Command> commands = new HashSet<>();
    Map<String, Command> name2CommandMap = new HashMap<>();

    Map<String, CommandKeyboardButtonExecutor> name2KeyboardExecutorMap = new HashMap<>();

    @Override
    public @Nullable Command getCommand(@NotNull String commandName) {
        return name2CommandMap.get(commandName.toLowerCase());
    }

    @Override
    public @Nullable CommandKeyboardButtonExecutor getKeyboardButtonExecutor(@NotNull String name) {
        return name2KeyboardExecutorMap.get(name.toLowerCase());
    }

    @Override
    public boolean register(@NotNull Command command) {
        if (!commands.add(command)) return false;

        name2CommandMap.put(command.getName().toLowerCase(), command);
        for (var alias : command.getAliases()) {
            name2CommandMap.put(alias.toLowerCase(), command);
        }

        return true;
    }

    @Override
    public boolean unregister(@NotNull Command command) {
        if (!commands.remove(command)) return false;

        name2CommandMap.remove(command.getName().toLowerCase(), command);
        for (var alias : command.getAliases()) {
            name2CommandMap.remove(alias.toLowerCase(), command);
        }

        return true;
    }

    @Override
    public boolean unregister(@NotNull String commandName) {
        var command = name2CommandMap.get(commandName.toLowerCase());

        return command != null && unregister(command);
    }

    @Override
    public @NotNull @Unmodifiable List<@NotNull Command> getCommands() {
        return List.copyOf(commands);
    }

    @Override
    public @NotNull CommandKeyboardButton registerKeyboardButton(
            @NotNull String name,
            @NotNull CommandKeyboardButtonExecutor executor
    ) {
        if (name2KeyboardExecutorMap.putIfAbsent(name, executor) != null) {
            throw new IllegalArgumentException("Keyboard button " + name + " already registered");
        }

        return new CommandKeyboardButton(keyboardButtonPayloadCodec, name);
    }
}
