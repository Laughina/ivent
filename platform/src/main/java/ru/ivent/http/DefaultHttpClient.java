package ru.ivent.http;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class DefaultHttpClient implements HttpClient {

    private static final ContentVisitor<String> CONTENT_TYPE = ContentTypeExtractingContentVisitor.getInstance();

    private static final byte[] CRLF = "\r\n"
        .getBytes(StandardCharsets.US_ASCII);
    private static final byte[] BOUNDARY_DELIMITER = "--"
        .getBytes(StandardCharsets.US_ASCII);

    private static final byte[] PART_CONTENT_DISPOSITION_START = "Content-Disposition: form-data; name=\""
            .getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PART_CONTENT_DISPOSITION_FILENAME = "\"; filename=\""
            .getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PART_CONTENT_DISPOSITION_END = "\"\r\n"
        .getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PART_CONTENT_TYPE = "Content-Type: "
        .getBytes(StandardCharsets.US_ASCII);

    int nThreads;

    @NonFinal
    volatile ExecutorService executor;

    @Override
    public void start() {
        executor = nThreads > 0
                ? Executors.newFixedThreadPool(nThreads)
                : Executors.newCachedThreadPool();
    }

    @Override
    public void stop() {
        executor.shutdown();
        executor = null;
    }

    @Override
    public @NotNull CompletableFuture<HttpResponse> post(String url, Content content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                content.accept(new RequestBootstrappingContentVisitor(urlConnection));

                urlConnection.getResponseCode();

                return getResponse(urlConnection);
            } catch (Exception exception) {
                throw new CompletionException(exception);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<HttpResponse> get(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(false);

                return getResponse(urlConnection);
            } catch (Exception exception) {
                throw new CompletionException(exception);
            }
        }, executor);
    }

    private @NotNull HttpResponse getResponse(@NotNull HttpURLConnection connection) throws IOException {
        connection.getResponseCode();

        InputStream stream = connection.getErrorStream();
        if (stream == null) {
            stream = connection.getInputStream();
        }

        return new HttpResponse(stream);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    private static final class RequestSenderContentVisitor extends ExceptionSneakyPropagatingContentVisitor<Void> {
        OutputStream output;

        @NonFinal
        byte[] buf;

        private byte[] touchBuffer() {
            byte[] buf;
            if ((buf = this.buf) == null) {
                this.buf = buf = new byte[8192];
            }
            return buf;
        }

        private static String getContentType(MultipartContent.@NotNull Part part) {
            return part.content().accept(CONTENT_TYPE);
        }

        @Override
        public @Nullable Void visitMultipart0(String contentType, @NotNull String boundary,
                                              @NotNull List<MultipartContent.Part> parts) throws IOException {
            var boundaryBytes = boundary.getBytes(StandardCharsets.US_ASCII);

            var os = output;
            os.write(BOUNDARY_DELIMITER);
            os.write(boundaryBytes);

            if (!parts.isEmpty()) {
                var iterator = parts.iterator();

                do {
                    os.write(CRLF);

                    var part = iterator.next();
                    os.write(PART_CONTENT_DISPOSITION_START);
                    os.write(part.name().getBytes(StandardCharsets.UTF_8));

                    String filename;
                    if ((filename = part.fileName()) != null) {
                        os.write(PART_CONTENT_DISPOSITION_FILENAME);
                        os.write(filename.getBytes(StandardCharsets.UTF_8));
                    }
                    os.write(PART_CONTENT_DISPOSITION_END);

                    String partContentType;
                    if ((partContentType = getContentType(part)) != null) {
                        os.write(PART_CONTENT_TYPE);
                        os.write(partContentType.getBytes(StandardCharsets.UTF_8));
                        os.write(CRLF);
                    }
                    os.write(CRLF);

                    part.content().accept(this);

                    os.write(CRLF);
                    os.write(BOUNDARY_DELIMITER);
                    os.write(boundaryBytes);
                } while (iterator.hasNext());
            }
            os.write(BOUNDARY_DELIMITER);
            os.write(CRLF);

            return null;
        }

        @Override
        public @Nullable Void visitInputStream0(String contentType, long size,
                                                @NotNull InputStream is) throws IOException {
            var buf = touchBuffer();
            int n;
            while ((n = is.read(buf)) != -1) {
                output.write(buf, 0, n);
            }
            return null;
        }

        @Override
        public Void visitFile0(String contentType, long size, Path path) throws IOException {
            try (var is = Files.newInputStream(path)) {
                return visitInputStream0(contentType, size, is);
            }
        }

        @Override
        public @Nullable Void visitBytes0(String contentType, byte[] bytes, int off, int len) throws IOException {
            output.write(bytes, off, len);
            return null;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    private static final class RequestBootstrappingContentVisitor
            extends ExceptionSneakyPropagatingContentVisitor<Void> {
        HttpURLConnection connection;

        private OutputStream configure(String contentType, long contentLength) throws IOException {
            connection.setRequestProperty("Content-Type", contentType);

            if (contentLength > 0L) {
                connection.setFixedLengthStreamingMode(contentLength);
            } else {
                connection.setChunkedStreamingMode(0);
            }

            return connection.getOutputStream();
        }

        @Override
        public Void visitInputStream0(String contentType, long size, InputStream is) throws IOException {
            try (var output = configure(contentType, size)) {
                return new RequestSenderContentVisitor(output)
                        .visitInputStream(contentType, size, is);
            }
        }

        @Override
        public Void visitMultipart0(String contentType, String boundary, List<MultipartContent.Part> parts) throws IOException {
            try (var output = configure(contentType, -1L)) {
                return new RequestSenderContentVisitor(output)
                        .visitMultipart0(contentType, boundary, parts);
            }
        }

        @Override
        public Void visitFile0(String contentType, long size, Path path) throws IOException {
            try (var output = configure(contentType, size)) {
                return new RequestSenderContentVisitor(output)
                        .visitFile(contentType, size, path);
            }
        }

        @Override
        public Void visitBytes0(String contentType, byte[] bytes, int off, int len) throws IOException {
            try (var output = configure(contentType, len)) {
                return new RequestSenderContentVisitor(output)
                        .visitBytes(contentType, bytes, off, len);
            }
        }
    }
}
