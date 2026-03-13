package ru.ivent.model;

import lombok.experimental.Accessors;
import ru.ivent.http.BytesContent;
import ru.ivent.http.EmbeddableContent;
import ru.ivent.http.FileContent;
import ru.ivent.http.InputStreamContent;
import ru.ivent.util.ByteArrayOutputStreamEx;
import ru.ivent.util.MimeUtils;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class AttachmentFactory {

    Executor executor;

    @Contract("_, _, _, _ -> new")
    public @NotNull Attachment fromByteArray(AttachmentType type, String fileName, String contentType, byte[] bytes) {
        return new BytesAttachment(type, fileName, contentType, bytes);
    }

    public @NotNull Attachment fromByteArray(AttachmentType type, String fileName, byte[] bytes) {
        var contentType = MimeUtils.determineContentType(fileName, new ByteArrayInputStream(bytes));

        return fromByteArray(type, fileName, contentType, bytes);
    }

    @Contract("_, _, _, _, _ -> new")
    public @NotNull Attachment fromInputStream(
            @NotNull AttachmentType type,
            @NotNull String fileName,
            @Nullable String contentType,
            long size,
            @NotNull Supplier<InputStream> is
    ) {
        return new InputStreamAttachment(executor, type, fileName, contentType, size, is);
    }

    @Contract("_, _, _ -> new")
    public @NotNull Attachment fromFile(@NotNull AttachmentType type, @NotNull Path path, @Nullable String contentType) {
        return new FileAttachment(executor, type, path, contentType);
    }

    @Contract("_, _, _, _ -> new")
    public @NotNull Attachment fromImage(
            @NotNull AttachmentType type,
            @NotNull String fileName,
            @NotNull String contentType,
            @NotNull BufferedImage image
    ) {
        return new BufferedImageAttachment(executor, type, fileName, contentType, image);
    }

    public Attachment fromImage(AttachmentType type, String format, BufferedImage image) {
        return fromImage(type, "image." + format, MimeUtils.getContentTypeByExtension(format), image);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    private static final class BufferedImageAttachment implements Attachment {

        Executor executor;

        @Getter
        AttachmentType type;

        @Getter
        String fileName;

        String contentType;

        BufferedImage image;

        @Override
        public @NotNull CompletableFuture<EmbeddableContent> createContent() {
            return CompletableFuture.supplyAsync(() -> {
                var writers = ImageIO.getImageWritersByMIMEType(contentType);
                if (!writers.hasNext()) {
                    throw new IllegalArgumentException("No image writer found for " + contentType);
                }

                try (var content = new ByteArrayOutputStreamEx();
                     var output = new MemoryCacheImageOutputStream(content)) {
                    var writer = writers.next();
                    writer.setOutput(output);

                    try {
                        writer.write(image);
                    } finally {
                        writer.dispose();
                    }

                    return new InputStreamContent(contentType, content.size(), content.toInputStream());
                } catch (IOException exception) {
                    throw new CompletionException(exception);
                }
            }, executor);
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    private static final class InputStreamAttachment implements Attachment {

        Executor executor;

        @Getter
        AttachmentType type;

        @Getter
        String fileName;

        String contentType;

        long size;

        Supplier<InputStream> is;

        @Override
        public @NotNull CompletableFuture<EmbeddableContent> createContent() {
            return CompletableFuture.supplyAsync(() -> {
                var is = this.is.get();

                String contentType;
                if ((contentType = this.contentType) == null) {
                    contentType = MimeUtils.determineContentType(fileName, is);
                }

                return new InputStreamContent(contentType, size, is);
            }, executor);
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    private static final class FileAttachment implements Attachment {

        Executor executor;

        @Getter
        AttachmentType type;

        Path path;

        String contentType;

        @Override
        public @NotNull String fileName() {
            return path.getFileName().toString();
        }

        @Override
        public @NotNull CompletableFuture<EmbeddableContent> createContent() {
            return CompletableFuture.supplyAsync(() -> {
                var path = this.path;
                try {
                    String contentType;
                    if ((contentType = this.contentType) == null) {
                        contentType = Files.probeContentType(path);
                    }
                    return new FileContent(contentType, Files.size(path), path);
                } catch (IOException exception) {
                    throw new CompletionException(exception);
                }
            }, executor);
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    private static final class BytesAttachment implements Attachment {

        @Getter
        AttachmentType type;

        @Getter
        String fileName;

        String contentType;

        byte[] bytes;

        @Override
        public @NotNull CompletableFuture<EmbeddableContent> createContent() {
            return CompletableFuture.completedFuture(new BytesContent(contentType, bytes));
        }
    }
}