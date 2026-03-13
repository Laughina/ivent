package ru.ivent.http;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Laughina
 */
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class InputStreamContent implements EmbeddableContent {

    @Getter
    String contentType;

    @Getter
    long size;

    InputStream is;

    @Override
    public <R> R accept(@NotNull ContentVisitor<R> visitor) {
        return visitor.visitInputStream(contentType, size, is);
    }

    @Override
    public ReadableByteChannel open() throws IOException {
        return Channels.newChannel(is);
    }
}
