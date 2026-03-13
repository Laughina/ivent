package ru.ivent.event;

import ru.ivent.model.InKeyboardCallback;
import ru.ivent.model.InMessage;
import ru.ivent.platform.Platform;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.slf4j.Logger;

import java.util.Set;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class SimpleEventDispatcher implements EventDispatcher {

    Logger logger;
    Set<EventHandler> eventHandlers;

    @Override
    public void message(Platform platform, InMessage inMessage) {
        for (var eventHandler : eventHandlers)
            try {
                eventHandler.onMessage(platform, inMessage);
            } catch (Exception exception) {
                logger.error("Failed to handle message by {}", eventHandlers, exception);
            }
    }

    @Override
    public void keyboardCallback(Platform platform, InKeyboardCallback inKeyboardCallback) {
        for (var eventHandler : eventHandlers)
            try {
                eventHandler.onKeyboardCallback(platform, inKeyboardCallback);
            } catch (Exception exception) {
                logger.error("Failed to handle keyboard callback by {}", eventHandlers, exception);
            }
    }
}
