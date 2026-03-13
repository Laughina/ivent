package ru.ivent.model;

import ru.ivent.platform.PlatformType;

import lombok.Value;

/**
 * @author Laughina
 */
@Value
public class Identity {

    long value;
    PlatformType platform;

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
