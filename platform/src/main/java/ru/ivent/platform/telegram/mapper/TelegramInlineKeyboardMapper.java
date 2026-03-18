package ru.ivent.platform.telegram.mapper;

import ru.ivent.model.InlineKeyboard;
import ru.ivent.platform.telegram.model.InlineKeyboardButton;
import ru.ivent.platform.telegram.model.InlineKeyboardMarkup;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Laughina
 */
@Mapper
public interface TelegramInlineKeyboardMapper {
    TelegramInlineKeyboardMapper INSTANCE = Mappers.getMapper(TelegramInlineKeyboardMapper.class);

    @Mapping(target = "inlineKeyboard", source = "buttons")
    InlineKeyboardMarkup mapKeyboard(InlineKeyboard keyboard);

    List<InlineKeyboardButton> mapKeyboardButtons(List<ru.ivent.model.InlineKeyboardButton> keyboardButtons);

    @Mapping(target = "text", source = "label")
    @Mapping(target = "callbackData", source = "data")
    @Mapping(target = "url", source = "url")
    InlineKeyboardButton mapKeyboardButton(ru.ivent.model.InlineKeyboardButton keyboardButton);
}
