package ru.ivent.http;

import lombok.SneakyThrows;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Laughina
 */
public abstract class ExceptionSneakyPropagatingContentVisitor<R> implements ContentVisitor<R> {

    protected abstract R visitInputStream0(String contentType, long size, InputStream is) throws Exception;

    protected abstract R visitMultipart0(String contentType, String boundary, List<MultipartContent.Part> parts) throws Exception;

    protected abstract R visitFile0(String contentType, long size, Path path) throws Exception;

    protected abstract R visitBytes0(String contentType, byte[] bytes, int off, int len) throws Exception;

    @SneakyThrows
    private static <R> R propagateSneaky(@NotNull Throwable throwable) {
        throw throwable;
    }

    @Override
    public final R visitInputStream(String contentType, long size, InputStream is) {
        try {
            return visitInputStream0(contentType, size, is);
        } catch (Exception exception) {
            return propagateSneaky(exception);
        }
    }

    @Override
    public final R visitMultipart(String contentType, String boundary, List<MultipartContent.Part> parts) {
        try {
            return visitMultipart0(contentType, boundary, parts);
        } catch (Exception exception) {
            return propagateSneaky(exception);
        }
    }

    @Override
    public final R visitFile(String contentType, long size, Path path) {
        try {
            return visitFile0(contentType, size, path);
        } catch (Exception exception) {
            return propagateSneaky(exception);
        }
    }

    @Override
    public final R visitBytes(String contentType, byte[] bytes, int off, int len) {
        try {
            return visitBytes0(contentType, bytes, off, len);
        } catch (Exception exception) {
            return propagateSneaky(exception);
        }
    }
}
