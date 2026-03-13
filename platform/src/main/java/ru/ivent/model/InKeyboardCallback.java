package ru.ivent.model;

import lombok.Value;

/**
 * @author Laughina
 */
@Value
public class InKeyboardCallback {

    Long replyMessageId;

    String data;
    IdentityHolder from;
    IdentityHolder chat;

    String id;
}
