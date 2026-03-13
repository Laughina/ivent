package ru.ivent.platform.telegram.method;

import ru.ivent.http.BytesContent;
import ru.ivent.http.Content;
import ru.ivent.http.EmbeddableContent;

import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class TelegramJsonMethodParams implements TelegramMethodParams {

    JsonMapper jsonMapper;

    Map<String, Object> params = new HashMap<>();

    @Override
    public void set(String field, Object value) {
        if (value == null) {
            return;
        }

        params.put(field, value);
    }

    @Override
    public void setFile(String field, String filename, EmbeddableContent fileContent) {
        throw new UnsupportedOperationException();
    }

    @Contract(" -> new")
    @SneakyThrows
    public @NotNull Content asContent() {
        return new BytesContent(
                "application/json",
                jsonMapper.writeValueAsBytes(params)
        );
    }
}
