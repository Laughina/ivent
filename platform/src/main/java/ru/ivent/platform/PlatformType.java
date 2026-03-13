package ru.ivent.platform;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * @author Laughina
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum PlatformType {

    TELEGRAM("Telegram"),
    VKONTAKTE("Vkontakte"),
    MAX("Max");

    String displayName;

}
