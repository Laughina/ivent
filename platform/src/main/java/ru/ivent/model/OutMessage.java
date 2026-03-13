package ru.ivent.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;

/**
 * @author Laughina
 */
@Value
public class OutMessage {

    Identity chat;
    Long reply;
    String text;
    String imageUrl;
    InlineKeyboard keyboard;
    Attachment attachment;

    boolean disableNotification;
    boolean keepForwardedMessages;
    boolean disableLinksParsing;

    Float latitude;
    Float longitude;

    public boolean hasAttachment() {
        return attachment != null;
    }

    public @NotNull Builder toBuilder() {
        return new Builder(
                chat, reply, text, imageUrl, keyboard, attachment,
                disableNotification, keepForwardedMessages, disableLinksParsing,
                latitude, longitude
        );
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(fluent = true)
    public static final class Builder {

        Identity chat;
        Long reply;

        @Setter
        String text;

        @Setter
        String imageUrl;

        @Setter
        InlineKeyboard keyboard;

        @Setter
        Attachment attachment;

        @Setter
        boolean disableNotification = true;

        @Setter
        boolean disableLinksParsing = false;

        @Setter
        boolean keepForwarded = true;

        @Setter
        Float latitude;

        @Setter
        Float longitude;

        public Builder reply(Identity chat, Long messageId) {
            this.chat = chat;
            this.reply = messageId;
            return this;
        }

        public Builder reply(@NotNull InMessage inMessage) {
            this.chat = inMessage.getChat().getIdentity();
            this.reply = inMessage.getId();
            return this;
        }

        public Builder reply(@NotNull InKeyboardCallback keyboardCallback) {
            this.chat = keyboardCallback.getChat().getIdentity();
            this.reply = keyboardCallback.getReplyMessageId();
            return this;
        }

        public Builder chat(@NotNull InMessage inMessage) {
            return chat(inMessage.getChat());
        }

        public Builder chat(Identity chat) {
            this.chat = chat;
            this.reply = null;
            return this;
        }

        public Builder chat(@NotNull IdentityHolder chat) {
            return chat(chat.getIdentity());
        }

        public @NotNull OutMessage build() {
            if (chat == null) {
                throw new IllegalStateException("Chat is required");
            }
            if (text == null && attachment == null && imageUrl == null) {
                throw new IllegalStateException("Text, imageUrl or attachment is required");
            }
            return new OutMessage(chat, reply, text, imageUrl, keyboard, attachment,
                    disableNotification, keepForwarded, disableLinksParsing, latitude, longitude);
        }
    }
}