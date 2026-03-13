package ru.ivent.http;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

/**
 * @author Laughina
 */
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class BytesContent implements EmbeddableContent {

    @Getter
    String contentType;

    byte[] bytes;
    int off;
    int len;

    public BytesContent(String contentType, @NotNull String string, Charset charset) {
        this(contentType, string.getBytes(charset));
    }

    public BytesContent(String contentType, byte[] bytes) {
        this(contentType, bytes, 0, bytes.length);
    }

    @Override
    public <R> R accept(@NotNull ContentVisitor<R> visitor) {
        return visitor.visitBytes(contentType, bytes, off, len);
    }

    @Override
    public ReadableByteChannel open() {
        return Channels.newChannel(new ByteArrayInputStream(bytes, off, len));
    }
}
