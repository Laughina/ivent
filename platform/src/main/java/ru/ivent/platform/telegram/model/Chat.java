package ru.ivent.platform.telegram.model;

import ru.ivent.model.IdentityHolder;
import ru.ivent.platform.PlatformType;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * @author whilein
 */
@Data
@Jacksonized
@SuperBuilder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class Chat implements IdentityHolder {

    long id;
    String type;
    String title;
    String firstName;
    String lastName;
    String username;
    boolean isForum;
    List<String> activeUsernames;
    String bio;
    String description;
    String inviteLink;
    int slowDelayMode;
    Message pinnedMessage;
    boolean hasHiddenMembers;
    boolean hasProtectedContent;
    boolean hasRestrictedVoiceAndVideoMessages;
    boolean hasPrivateForwards;
    boolean joinByRequest;
    boolean joinToSendMessages;
    int linkedChatId;
    boolean canSetStickerSet;
    String stickerSetName;
    boolean hasAggressiveAntiSpamEnabled;
    int messageAutoDeleteTime;
    String emojiStatusCustomEmojiId;
    int emojiStatusExpirationDate;

    @Override
    public long getValue() {
        return id;
    }

    @Override
    public boolean isChat() {
        return type.equals("group") || type.equals("supergroup");
    }

    @Override
    public boolean isBot() {
        return false;
    }

    @Override
    public boolean isUser() {
        return type.equals("private");
    }

    @Override
    public PlatformType getPlatform() {
        return PlatformType.TELEGRAM;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
