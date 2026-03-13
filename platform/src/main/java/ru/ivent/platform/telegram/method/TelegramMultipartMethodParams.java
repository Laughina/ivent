package ru.ivent.platform.telegram.method;

import ru.ivent.http.BytesContent;
import ru.ivent.http.Content;
import ru.ivent.http.EmbeddableContent;
import ru.ivent.http.MultipartContent;

import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class TelegramMultipartMethodParams implements TelegramMethodParams {

    JsonMapper jsonMapper;

    MultipartContent.Builder multiPartContent = new MultipartContent.Builder();

    @Override
    @SneakyThrows
    public void set(String field, Object value) {
        if (value == null) {
            return;
        }

        multiPartContent.addPart(new MultipartContent.Part(field, null,
                !(value instanceof String) && !(value instanceof Number)
                ? new BytesContent(null, jsonMapper.writeValueAsBytes(value))
                : new BytesContent(null, value.toString(), StandardCharsets.UTF_8)));
    }

    @Override
    public void setFile(String field, String filename, EmbeddableContent content) {
        multiPartContent.addPart(new MultipartContent.Part(field, filename, content));
    }

    @Contract(" -> new")
    @SneakyThrows
    public @NotNull Content asContent() {
        return multiPartContent.build();
    }
}
