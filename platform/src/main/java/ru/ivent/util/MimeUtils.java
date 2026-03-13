package ru.ivent.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.experimental.UtilityClass;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Laughina
 */
@UtilityClass
public class MimeUtils {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final Map<String, String> EXTENSION_TO_MIME_TYPE;

    static {
        EXTENSION_TO_MIME_TYPE = loadMimeTypes();
    }

    private static @Nullable Map<String, String> loadMimeTypes() {
        InputStream is = MimeUtils.class.getClassLoader().getResourceAsStream("mime_types.json");
        if (is == null) return null;

        Map<String, String[]> mimeTypes;

        try {
            mimeTypes = new JsonMapper().readValue(is, new TypeReference<HashMap<String, String[]>>() {});
        } catch (IOException exception) {
            return null;
        }

        var extensionToMimeTypes = new HashMap<String, String>();

        for (var entry : mimeTypes.entrySet()) {
            for (var extension : entry.getValue()) {
                extensionToMimeTypes.put(extension, entry.getKey());
            }
        }

        return extensionToMimeTypes;
    }

    public String getContentTypeByExtension(String extension) {
        var extensionToMimeType = EXTENSION_TO_MIME_TYPE;
        return extensionToMimeType != null
                ? extensionToMimeType.getOrDefault(extension, DEFAULT_CONTENT_TYPE)
                : DEFAULT_CONTENT_TYPE;
    }

    private @NotNull String getExtension(@NotNull String filename) {
        var extension = filename.lastIndexOf('.');
        if (extension == -1) return "";

        return filename.substring(extension + 1).toLowerCase();
    }

    public String determineContentType(String filename, InputStream content) {
        String contentType = null;

        try {
            contentType = URLConnection.guessContentTypeFromStream(content);
        } catch (IOException ignored) {
        }

        if (contentType == null) {
            contentType = getContentTypeByExtension(getExtension(filename));
        }

        return contentType;
    }
}
