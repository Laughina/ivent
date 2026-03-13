package ru.ivent.http;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Laughina
 */
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class FileContent implements EmbeddableContent {

    @Getter
    String contentType;

    @Getter
    long size;

    Path path;

    @Override
    public <R> R accept(@NotNull ContentVisitor<R> visitor) {
        return visitor.visitFile(contentType, size, path);
    }

    @Override
    public @NotNull ReadableByteChannel open() throws IOException {
        return Files.newByteChannel(path);
    }
}
