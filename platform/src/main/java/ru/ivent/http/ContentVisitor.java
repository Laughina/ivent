package ru.ivent.http;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Laughina
 */
public interface ContentVisitor<R> {

    R visitInputStream(String contentType, long size, InputStream is);

    R visitMultipart(String contentType, String boundary, List<MultipartContent.Part> parts);

    R visitFile(String contentType, long size, Path path);

    R visitBytes(String contentType, byte[] bytes, int off, int len);
}
