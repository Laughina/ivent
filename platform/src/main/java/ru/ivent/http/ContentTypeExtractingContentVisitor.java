package ru.ivent.http;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Laughina
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContentTypeExtractingContentVisitor implements ContentVisitor<String> {

    private static final ContentVisitor<String> INSTANCE = new ContentTypeExtractingContentVisitor();

    public static ContentVisitor<String> getInstance() {
        return INSTANCE;
    }

    public static @NotNull String getMultipartType(String boundary) {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public String visitInputStream(String contentType, long size, InputStream is) {
        return contentType;
    }

    @Override
    public String visitMultipart(String contentType, String boundary, List<MultipartContent.Part> parts) {
        return contentType;
    }

    @Override
    public String visitFile(String contentType, long size, Path path) {
        return contentType;
    }

    @Override
    public String visitBytes(String contentType, byte[] bytes, int off, int len) {
        return contentType;
    }
}
