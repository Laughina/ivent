package ru.ivent.platform.telegram.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Value;

/**
 * @author Laughina
 */
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputMedia {

    String type;
    String media;
    String caption;

}
