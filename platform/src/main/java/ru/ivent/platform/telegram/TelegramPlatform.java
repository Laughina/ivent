package ru.ivent.platform.telegram;

import org.jetbrains.annotations.Contract;
import ru.ivent.event.EventDispatcher;
import ru.ivent.http.HttpResponse;
import ru.ivent.model.Attachment;
import ru.ivent.model.IdentityHolder;
import ru.ivent.model.IdentityName;
import ru.ivent.model.ImageDimensions;
import ru.ivent.model.InKeyboardCallback;
import ru.ivent.model.InMessage;
import ru.ivent.model.InlineKeyboard;
import ru.ivent.model.OutMessage;
import ru.ivent.model.Photo;
import ru.ivent.model.PhotoSize;
import ru.ivent.model.SentMessage;
import ru.ivent.platform.Platform;
import ru.ivent.platform.PlatformType;
import ru.ivent.platform.telegram.mapper.TelegramInlineKeyboardMapper;
import ru.ivent.platform.telegram.mapper.TelegramMessageMapper;
import ru.ivent.platform.telegram.method.TelegramSend;
import ru.ivent.platform.telegram.model.CallbackQuery;
import ru.ivent.platform.telegram.model.Chat;
import ru.ivent.platform.telegram.model.File;
import ru.ivent.platform.telegram.model.LinkPreviewOptions;
import ru.ivent.platform.telegram.model.Message;
import ru.ivent.platform.telegram.model.Update;
import ru.ivent.platform.telegram.model.User;
import ru.ivent.util.FutureUtils;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.slf4j.Logger;

import java.io.InputStream;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class TelegramPlatform implements Platform {

    @Getter
    Logger logger;

    @Getter
    TelegramClient telegramClient;

    EventDispatcher eventDispatcher;

    @Getter
    @NonFinal
    IdentityHolder identity;

    private void handleUpdate(@NotNull Update update) {
        Message message;
        if ((message = update.getMessage()) != null) {
            eventDispatcher.message(this, TelegramMessageMapper.INSTANCE.mapToMessage(message));
        }

        CallbackQuery query;
        if ((query = update.getCallbackQuery()) != null) {
            eventDispatcher.keyboardCallback(this, TelegramMessageMapper.INSTANCE.mapToKeyboardCallback(query));
        }
    }

    @Override
    public @NotNull CompletableFuture<IdentityName> getName(IdentityHolder identity) {
        if (identity instanceof User user) {
            return completedFuture(new IdentityName(user.getFirstName(), user.getLastName()));
        } else if (identity instanceof Chat) {
            throw new IllegalArgumentException("Cannot get display name of chat identity");
        } else {
            throw new IllegalArgumentException("Identity platform is not Telegram");
        }
    }

    @Override
    public @NotNull CompletableFuture<HttpResponse> getAvatar(@NotNull IdentityHolder identity, PhotoSize photoSize) {
        if (identity.isChat()) {
            throw new IllegalArgumentException("Cannot get avatar of chat identity");
        }

        if (!(identity instanceof User)) {
            throw new IllegalArgumentException("Identity platform is not Telegram");
        }

        return telegramClient.getUserProfilePhotos()
                .userId(identity.getValue())
                .limit(PhotoSize.VALUES.size())
                .make()
                .thenCompose(profile -> {
                    var photos = profile.getPhotos();
                    if (photos.isEmpty()) return completedFuture(new HttpResponse(InputStream.nullInputStream()));

                    var sizes = profile.getPhotos().getFirst();
                    var firstPhoto = sizes.get(photoSize.ordinal());

                    return telegramClient.getFile()
                            .fileId(firstPhoto.getFileId())
                            .make()
                            .thenApply(File::getFilePath)
                            .thenCompose(telegramClient::getFile);
                });
    }

    @Contract("null -> fail")
    @Override
    public @NotNull String formatLinkToIdentity(IdentityHolder identity) {
        if (!(identity instanceof User)) {
            throw new IllegalArgumentException("Identity platform is not Telegram");
        }

        return "https://t.me/" + ((User) identity).getUsername();
    }

    @Override
    public PlatformType getType() {
        return PlatformType.TELEGRAM;
    }

    private @NotNull CompletableFuture<TelegramSend<?>> sendAttachment(@NotNull Attachment attachment, String caption) {
        return attachment.createContent()
                .thenApply(content -> switch (attachment.type()) {
                    case PHOTO -> telegramClient.sendPhoto().photo(attachment.fileName(), content)
                            .caption(caption).parseMode("HTML");
                    case DOCUMENT -> telegramClient.sendDocument().document(attachment.fileName(), content)
                            .caption(caption).parseMode("HTML");
                });
    }

    @Override
    public @NotNull CompletableFuture<SentMessage> sendMessage(@NotNull OutMessage message) {
        var peer = message.getChat();
        if (peer.getPlatform() != PlatformType.TELEGRAM) {
            throw new IllegalArgumentException("Chat platform is not Telegram");
        }

        var attachment = message.getAttachment();
        var imageUrl = message.getImageUrl();

        CompletableFuture<TelegramSend<?>> sendFuture;
        if (attachment != null) {
            sendFuture = sendAttachment(attachment, message.getText());
        } else if (imageUrl != null) {
            sendFuture = completedFuture(telegramClient.sendPhoto()
                    .photoUrl(imageUrl)
                    .caption(message.getText())
                    .parseMode("HTML"));
        } else {
            sendFuture = completedFuture(telegramClient.sendMessage()
                    .text(message.getText())
                    .parseMode("HTML"));
        }
        return sendFuture
                .thenCompose(send -> {
                    send.chatId(peer.getValue());

                    var reply = message.getReply();
                    if (reply != null) {
                        send.replyToMessageId(reply);
                    }

                    var keyboard = message.getKeyboard();
                    if (keyboard != null) {
                        send.replyMarkup(TelegramInlineKeyboardMapper.INSTANCE.mapKeyboard(keyboard));
                    }

                    if (message.isDisableLinksParsing()) {
                        send.linkPreviewOptions(LinkPreviewOptions.disabled());
                    }

                    if (message.isDisableNotification()) {
                        send.disableNotification(true);
                    }

                    return send.make();
                })
                .thenApply(m -> new SentMessage(this, m.getMessageId(), m.getMessageId(), message));
    }

    @Override
    public CompletableFuture<Void> editAttachment(SentMessage message, @NotNull Attachment attachment) {
        var type = attachment.type().toString().toLowerCase();

        return FutureUtils.asVoid(attachment.createContent()
                .thenCompose(content -> telegramClient.editMessageMedia()
                        .messageId(message.getMessageId())
                        .chatId(message.getOutMessage().getChat().getValue())
                        .media(type, attachment.fileName(), content)
                        .make()));
    }

    @Override
    public CompletableFuture<Void> editText(@NotNull SentMessage message, String text) {
        var oldMessage = message.getOutMessage();

        return editMessageContent(message.getMessageId(), oldMessage.getChat().getValue(),
                text, null, oldMessage.getAttachment(), null, null, true);
    }

    @Override
    public CompletableFuture<Void> editMessage(@NotNull SentMessage message, @NotNull OutMessage newMessage) {
        var oldMessage = message.getOutMessage();

        return editMessageContent(message.getMessageId(), oldMessage.getChat().getValue(),
                newMessage.getText(), newMessage.getImageUrl(),
                oldMessage.getAttachment(), newMessage.getAttachment(),
                newMessage.getKeyboard(), newMessage.isDisableLinksParsing());
    }

    @Override
    public CompletableFuture<Void> editMessage(@NotNull InKeyboardCallback keyboardCallback, @NotNull OutMessage newMessage) {
        return editMessageContent(keyboardCallback.getReplyMessageId(), keyboardCallback.getChat().getValue(),
                newMessage.getText(), newMessage.getImageUrl(),
                null, newMessage.getAttachment(),
                newMessage.getKeyboard(), newMessage.isDisableLinksParsing());
    }

    private CompletableFuture<Void> editMessageContent(
            long messageId,
            long chatId,
            String text,
            String imageUrl,
            Attachment oldAttachment,
            Attachment attachment,
            InlineKeyboard keyboard,
            boolean disableLinksParsing
    ) {

        if (attachment != null) {
            var editMessageMedia = telegramClient.editMessageMedia()
                    .messageId(messageId)
                    .chatId(chatId);

            if (keyboard != null) {
                editMessageMedia.replyMarkup(TelegramInlineKeyboardMapper.INSTANCE.mapKeyboard(keyboard));
            }

            var type = attachment.type().toString().toLowerCase();

            return FutureUtils.asVoid(attachment.createContent()
                    .thenCompose(content -> editMessageMedia
                            .media(type, attachment.fileName(), content, text)
                            .make()));
        }


        if (imageUrl != null) {
            var editMessageMedia = telegramClient.editMessageMedia()
                    .messageId(messageId)
                    .chatId(chatId)
                    .mediaUrl("photo", imageUrl, text);

            if (keyboard != null) {
                editMessageMedia.replyMarkup(TelegramInlineKeyboardMapper.INSTANCE.mapKeyboard(keyboard));
            }

            return FutureUtils.asVoid(editMessageMedia.make());
        }


        var telegramEdit = (oldAttachment == null)
                ? telegramClient.editMessageText().text(text).parseMode("HTML")
                : telegramClient.editMessageCaption().caption(text).parseMode("HTML");

        if (keyboard != null) {
            telegramEdit.replyMarkup(TelegramInlineKeyboardMapper.INSTANCE.mapKeyboard(keyboard));
        }

        if (disableLinksParsing) {
            telegramEdit.linkPreviewOptions(LinkPreviewOptions.disabled());
        }

        return FutureUtils.asVoid(telegramEdit.messageId(messageId)
                .chatId(chatId)
                .make());
    }

    @Override
    public CompletableFuture<Photo> getPhoto(
            @NotNull InMessage message,
            Predicate<ImageDimensions> filter,
            Comparator<ImageDimensions> maxComparator
    ) {
        if (!(message.getRef() instanceof Message ref)) {
            throw new IllegalArgumentException("Source platform is not Telegram");
        }

        var photo = ref.getPhoto();
        if (photo == null) {
            return completedFuture(null);
        }

        var bestPhoto = photo.stream()
                .map(TelegramPlatformPhotoDimensions::new)
                .filter(filter)
                .max(maxComparator)
                .orElse(null);

        if (bestPhoto == null) {
            return completedFuture(null);
        }

        var photoSize = bestPhoto.photoSize;
        return telegramClient.getFile()
                .fileId(photoSize.getFileId())
                .make()
                .thenApply(file -> new Photo(
                        telegramClient.getUrlToFile(file.getFilePath()),
                        photoSize.getWidth(),
                        photoSize.getHeight()));
    }

    @Override
    public CompletableFuture<Void> deleteMessage(@NotNull InKeyboardCallback keyboardCallback) {
        return FutureUtils.asVoid(telegramClient.deleteMessage()
                .chatId(keyboardCallback.getChat().getValue())
                .messageId(keyboardCallback.getReplyMessageId())
                .make());
    }

    @Override
    public CompletableFuture<Void> answerCallback(@NotNull InKeyboardCallback keyboardCallback, @Nullable String text) {
        var answerCallbackQuery = telegramClient.answerCallbackQuery()
                .queryId(keyboardCallback.getId());

        if (text != null) {
            if (text.length() > 200) {
                throw new IllegalArgumentException(String.format("Text too long, %s > 200", text.length()));
            }

            answerCallbackQuery.text(text);
        }

        return FutureUtils.asVoid(answerCallbackQuery.make());
    }

    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static class TelegramPlatformPhotoDimensions implements ImageDimensions {

        ru.ivent.platform.telegram.model.PhotoSize photoSize;

        @Override
        public int getWidth() {
            return photoSize.getWidth();
        }

        @Override
        public int getHeight() {
            return photoSize.getHeight();
        }
    }

    @Override
    public void run() {
        try {
            var user = telegramClient.getMe()
                    .make()
                    .get();

            this.identity = user;

            logger.info("Waiting for updates in bot @{} (id: {})", user.getUsername(), user.getId());

            var telegram = new TelegramLongPoll(logger, telegramClient);
            telegram.start(this::handleUpdate);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            logger.error("Failed to start Telegram LongPoll", exception);
        }
    }
}
