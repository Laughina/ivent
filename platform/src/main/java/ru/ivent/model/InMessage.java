package ru.ivent.model;

import org.jetbrains.annotations.ApiStatus;

import lombok.Value;

import java.util.List;

/**
 * @author Laughina
 */
@Value
public class InMessage {

    long id;
    String text;
    IdentityHolder from;
    IdentityHolder chat;
    InMessage reply;
    List<InMessage> forwarded;
    long date;
    boolean hasPhoto;

    @ApiStatus.Internal
    Object ref;
}
