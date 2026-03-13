package ru.ivent.platform.telegram.model;

import ru.ivent.model.IdentityHolder;
import ru.ivent.platform.PlatformType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Laughina
 */
@Value
@Builder
@Jacksonized
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements IdentityHolder {

    long id;
    boolean isBot;
    String firstName;
    String lastName;
    String username;
    String languageCode;
    boolean isPremium;
    boolean addedToAttachmentMenu;
    boolean canJoinGroups;
    boolean canReadAllGroupMessages;
    boolean supportsInlineQueries;

    @Override
    public long getValue() {
        return id;
    }

    @Override
    public boolean isChat() {
        return false;
    }

    @Override
    public boolean isUser() {
        return !isBot;
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
