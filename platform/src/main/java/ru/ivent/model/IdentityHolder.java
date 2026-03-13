package ru.ivent.model;

import ru.ivent.platform.PlatformType;

/**
 * @author Laughina
 */
public interface IdentityHolder {

    default Identity getIdentity() {
        return new Identity(getValue(), getPlatform());
    }

    long getValue();

    PlatformType getPlatform();

    boolean isChat();

    boolean isBot();

    boolean isUser();
}
