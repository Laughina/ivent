package ru.ivent.platform.telegram.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * @author Laughina
 */
@Value
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputMedia {

    String type;
    String media;
    String caption;

    @JsonProperty("parse_mode")
    String parseMode;

    public InputMedia(String type, String media, String caption) {
        this(type, media, caption, null);
    }
}
