package ru.ivent.util;

import lombok.experimental.UtilityClass;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Laughina
 */
@UtilityClass
public class EncodeUtils {

    public String encodeURL(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
