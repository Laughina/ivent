package ru.ivent.model;

import ru.ivent.platform.Platform;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SentMessage {

    Platform platform;

    @Getter
    long messageId;

    @Getter
    @Nullable
    Long chatMessageId;

    @Getter
    OutMessage outMessage;

    public CompletableFuture<Void> editText(String text) {
        return platform.editText(this, text);
    }

    public CompletableFuture<Void> editAttachment(Attachment attachment) {
        return platform.editAttachment(this, attachment);
    }

    public CompletableFuture<Void> editMessage(OutMessage newMessage) {
        return platform.editMessage(this, newMessage);
    }
}
