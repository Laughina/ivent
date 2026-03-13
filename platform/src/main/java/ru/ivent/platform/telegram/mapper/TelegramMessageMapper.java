package ru.ivent.platform.telegram.mapper;

import ru.ivent.model.InKeyboardCallback;
import ru.ivent.model.InMessage;
import ru.ivent.platform.telegram.model.CallbackQuery;
import ru.ivent.platform.telegram.model.Message;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Laughina
 */
@Mapper
public interface TelegramMessageMapper {

    TelegramMessageMapper INSTANCE = Mappers.getMapper(TelegramMessageMapper.class);

    @Mapping(target = "id", source = "messageId")
    @Mapping(target = "reply", source = "replyToMessage")
    @Mapping(target = "forwarded", source = "message", qualifiedByName = "mapReplyToForwardedMessage")
    @Mapping(target = "ref", source = ".", qualifiedByName = "mapToRef")
    @Mapping(target = "text", source = ".", qualifiedByName = "mapText")
    @Mapping(target = "hasPhoto", source = ".", qualifiedByName = "hasPhoto")
    InMessage mapToMessage(Message message);

    @Mapping(target = "replyMessageId", source = "message.messageId")
    @Mapping(target = "chat", source = "message.chat")
    InKeyboardCallback mapToKeyboardCallback(CallbackQuery callbackQuery);

    @Named("mapReplyToForwardedMessage")
    default List<InMessage> mapReplyToForwardedMessage(@NotNull Message message) {
        Message replyToMessage;
        if ((replyToMessage = message.getReplyToMessage()) != null) {
            return Collections.singletonList(mapToMessage(replyToMessage));
        }

        return Collections.emptyList();
    }

    @Named("mapToRef")
    default Object mapToRef(Message message) {
        return message;
    }

    @Named("mapText")
    default String mapText(@NotNull Message message) {
        return message.getText() == null
                ? message.getCaption()
                : message.getText();
    }

    @Named("hasPhoto")
    default boolean hasPhoto(@NotNull Message message) {
        return message.getPhoto() != null && !message.getPhoto().isEmpty();
    }
}
