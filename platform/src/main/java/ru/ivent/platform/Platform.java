package ru.ivent.platform;

import ru.ivent.http.HttpResponse;
import ru.ivent.model.*;

import org.slf4j.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * @author Laughina
 */
public interface Platform extends Runnable {

    Logger getLogger();

    IdentityHolder getIdentity();

    PlatformType getType();

    CompletableFuture<SentMessage> sendMessage(@NotNull OutMessage message);

    CompletableFuture<Void> editText(SentMessage message, String text);

    CompletableFuture<Void> editAttachment(SentMessage message, Attachment attachment);

    CompletableFuture<Void> editMessage(SentMessage message, OutMessage newMessage);

    CompletableFuture<Void> editMessage(InKeyboardCallback message, OutMessage newMessage);

    CompletableFuture<Void> deleteMessage(InKeyboardCallback keyboardCallback);

    CompletableFuture<IdentityName> getName(IdentityHolder identity);

    CompletableFuture<HttpResponse> getAvatar(IdentityHolder identity, PhotoSize photoSize);

    String formatLinkToIdentity(IdentityHolder identity);

    CompletableFuture<Photo> getPhoto(InMessage message, Predicate<ImageDimensions> filter,
                                      Comparator<ImageDimensions> sizeComparator);

    CompletableFuture<Void> answerCallback(InKeyboardCallback keyboardCallback, @Nullable String text);
}
