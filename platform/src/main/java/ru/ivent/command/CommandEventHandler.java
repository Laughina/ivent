package ru.ivent.command;

import ru.ivent.event.EventHandler;
import ru.ivent.model.InKeyboardCallback;
import ru.ivent.model.InMessage;
import ru.ivent.platform.Platform;

import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CommandEventHandler implements EventHandler {

    private static final Pattern SPACE = Pattern.compile(" +");

    CommandManager commandManager;
    JsonMapper jsonMapper;

    @Override
    public void onKeyboardCallback(Platform platform, @NotNull InKeyboardCallback inKeyboardCallback) {
        var payloadOpt = commandManager.getKeyboardButtonPayloadCodec().deserialize(inKeyboardCallback.getData());
        if (!payloadOpt.isPresent()) return;

        var payload = payloadOpt.get();
        var name = payload.getName();
        var executor = commandManager.getKeyboardButtonExecutor(name);
        if (executor == null) return;

        executor.execute(new KeyboardContext(platform, name, payload.getArgs(), inKeyboardCallback, jsonMapper));
    }

    @Override
    public void onMessage(Platform platform, @NotNull InMessage inMessage) {
        var text = inMessage.getText();
        if (text == null || text.isEmpty() || text.charAt(0) != '/') return;

        var params = SPACE.split(text.substring(1));

        var commandName = params[0];
        var command = commandManager.getCommand(commandName);
        if (command == null) return;

        command.execute(new CommandContext(platform, commandName, 1, params, inMessage));
    }
}
