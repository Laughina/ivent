package ru.ivent.http;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Laughina
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class HttpResponse implements AutoCloseable {

    InputStream content;

    @Override
    public void close() throws IOException {
        content.close();
    }
}
