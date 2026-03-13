package ru.ivent.event;

import ru.ivent.model.InKeyboardCallback;
import ru.ivent.model.InMessage;
import ru.ivent.platform.Platform;

/**
 * @author Laughina
 */
public interface EventHandler {

    default void onMessage(Platform platform, InMessage inMessage) throws Exception {
    }

    default void onKeyboardCallback(Platform platform, InKeyboardCallback inKeyboardCallback) throws Exception {
    }
}
