package ru.ivent.platform.telegram.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Laughina
 */
@Value
@Builder
@Jacksonized
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkPreviewOptions {

    public static LinkPreviewOptions disabled() {
        return LinkPreviewOptions.builder()
                .disabled(true)
                .build();
    }

    @JsonProperty("is_disabled")
    boolean disabled;

    boolean preferSmallMedia;

    boolean preferLargeMedia;

    boolean showAboveText;

    String url;
}
