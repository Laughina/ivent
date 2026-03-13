package ru.ivent.command;

import java.util.StringJoiner;

/**
 * @author Laughina
 */
public interface ArgumentProvider {

    String argument(int i);

    int argumentCount();

    default String joinArguments(int start) {
        var joiner = new StringJoiner(" ");

        for (int i = start, j = argumentCount(); i < j; i++) {
            joiner.add(argument(i));
        }

        return joiner.toString();
    }
}
