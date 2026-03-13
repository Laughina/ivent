package ru.ivent.model;

import lombok.Value;

/**
 * @author Laughina
 */
@Value
public class Photo implements ImageDimensions {

    String url;
    int width;
    int height;
}
