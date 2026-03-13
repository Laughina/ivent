package ru.ivent.model;

import lombok.Value;
import lombok.experimental.NonFinal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Laughina
 */
@Value
public class InlineKeyboard {

    @Unmodifiable List<@Unmodifiable List<InlineKeyboardButton>> buttons;

    public static final class Builder {

        @NonFinal
        List<InlineKeyboardButton> rowButtons = new ArrayList<>();

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        public Builder button(String label, String data) {
            return button(label, data, null);
        }

        public Builder button(String label, String data, String color) {
            button(new InlineKeyboardButton(label, data, color));
            return this;
        }

        public Builder button(InlineKeyboardButton button) {
            rowButtons.add(button);
            return this;
        }

        public Builder row() {
            buttons.add(Collections.unmodifiableList(new ArrayList<>(rowButtons)));
            rowButtons = new ArrayList<>();
            return this;
        }

        public @NotNull InlineKeyboard build() {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>(this.buttons);

            if (!rowButtons.isEmpty()) {
                buttons.add(List.copyOf(rowButtons));
            }

            return new InlineKeyboard(Collections.unmodifiableList(buttons));
        }
    }
}
