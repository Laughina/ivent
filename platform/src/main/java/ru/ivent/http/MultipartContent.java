package ru.ivent.http;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Laughina
 */
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class MultipartContent implements Content {

    @Getter
    String contentType;

    @Getter
    String boundary;

    List<Part> parts;

    public MultipartContent(String boundary, List<Part> parts) {
        this.contentType = "multipart/form-data; boundary=" + boundary;
        this.boundary = boundary;
        this.parts = new ArrayList<>(parts);
    }

    private static @NotNull String makeBoundary() {
        StringBuilder builder = new StringBuilder("IVENTBOT");
        builder.append(Long.toString(System.identityHashCode(builder), 36));
        builder.append(Long.toString(System.identityHashCode(Thread.currentThread()), 36));
        builder.append(Long.toString(System.nanoTime(), 36));
        return builder.toString();
    }

    @Override
    public <R> R accept(@NotNull ContentVisitor<R> visitor) {
        return visitor.visitMultipart(contentType, boundary, parts);
    }

    public record Part(String name, String fileName, EmbeddableContent content) {
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static final class Builder {

        @Setter
        String boundary;

        final List<Part> parts = new ArrayList<>();

        public Builder addPart(Part part) {
            this.parts.add(part);
            return this;
        }

        public @NotNull MultipartContent build() {
            String boundary;
            if ((boundary = this.boundary) == null) {
                boundary = makeBoundary();
            }

            return new MultipartContent(boundary, parts);
        }
    }
}
