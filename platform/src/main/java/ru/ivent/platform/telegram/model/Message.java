package ru.ivent.platform.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * @author Laughina
 */
@Value
@Builder
@Jacksonized
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

    long messageId;
    long messageThreadId;
    User from;
    Chat chat;
    Chat senderChat;
    long date;
    long forwardDate;
    String text;
    String caption;
    Message replyToMessage;
    List<PhotoSize> photo;
}
