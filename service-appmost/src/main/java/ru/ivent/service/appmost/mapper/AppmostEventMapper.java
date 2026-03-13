package ru.ivent.service.appmost.mapper;

import ru.ivent.service.appmost.dto.AppmostEvent;
import ru.ivent.service.model.Event;

import java.time.Instant;

/**
 * Converts a raw {@link AppmostEvent} DTO into the unified {@link Event} model.
 *
 * @author Laughina
 */
public final class AppmostEventMapper {

    private static final String SOURCE      = "appmost";
    private static final String BASE_URL    = "https://api.appmost.ru";
    private static final String CURRENCY    = "RUB";

    private AppmostEventMapper() {}

    public static Event toEvent(AppmostEvent dto) {
        return Event.builder()
                .id(SOURCE + ":" + dto.id())
                .source(SOURCE)
                .title(dto.title())
                .description(dto.description())
                .imageUrl(dto.poster())
                .eventUrl(resolveUrl(dto.url()))
                .venue(dto.place() != null ? dto.place().title() : null)
                .category(resolveCategory(dto))
                .startTime(toInstant(dto.dateStart()))
                .endTime(toInstant(dto.dateEnd()))
                .minPrice(dto.minPrice())
                .currency(CURRENCY)
                .ageRestriction(dto.ageRestriction())
                .tags(dto.tags())
                .cachedAt(Instant.now())
                .build();
    }

    // -------------------------------------------------------------------------

    private static Instant toInstant(Long epochSeconds) {
        return epochSeconds != null ? Instant.ofEpochSecond(epochSeconds) : null;
    }

    private static String resolveUrl(String url) {
        if (url == null) return null;
        return url.startsWith("http") ? url : BASE_URL + url;
    }

    private static String resolveCategory(AppmostEvent dto) {
        if (dto.category() == null) return null;
        String alias = dto.category().alias();
        return alias != null ? alias.toUpperCase() : dto.category().title();
    }
}
