package ru.ivent.command;

import ru.ivent.model.IdentityHolder;
import ru.ivent.model.InMessage;
import ru.ivent.model.OutMessage;
import ru.ivent.model.SentMessage;
import ru.ivent.platform.Platform;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.CompletableFuture;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class CommandContext implements ArgumentProvider {

    @Getter
    Platform platform;
    @Getter
    String name;
    int argumentOffset;
    @Getter
    String[] arguments;
    @Getter
    InMessage message;

    public IdentityHolder chat() {
        return message.getChat();
    }

    public IdentityHolder from() {
        return message.getFrom();
    }

    @Override
    public String argument(int i) {
        return arguments[i + argumentOffset];
    }

    @Override
    public int argumentCount() {
        return arguments.length - argumentOffset;
    }

    public CompletableFuture<SentMessage> sendMessage(OutMessage message) {
        return platform.sendMessage(message);
    }
}
