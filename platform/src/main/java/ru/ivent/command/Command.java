package ru.ivent.command;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * @author Laughina
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class Command implements CommandExecutor {

    String name;
    @Unmodifiable List<String> aliases;

    public Command(String name, List<String> aliases) {
        this.name = name;
        this.aliases = List.copyOf(aliases);
    }
}
