package ru.ivent.model;

import java.util.Arrays;
import java.util.List;

/**
 * @author Laughina
 */
public enum PhotoSize {

    MINIMAL,

    NORMAL,

    MAXIMUM;

    public static final List<PhotoSize> VALUES = Arrays.asList(PhotoSize.values());
}
