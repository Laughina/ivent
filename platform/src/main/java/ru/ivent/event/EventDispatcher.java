package ru.ivent.event;

import ru.ivent.model.InKeyboardCallback;
import ru.ivent.model.InMessage;
import ru.ivent.platform.Platform;

/**
 * @author Laughina
 */
public interface EventDispatcher {

    void message(Platform platform, InMessage inMessage);

    void keyboardCallback(Platform platform, InKeyboardCallback inKeyboardCallback);
}
