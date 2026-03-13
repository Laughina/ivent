package ru.ivent.service.afishaykt.mapper;

import ru.ivent.service.afishaykt.dto.AfishaYktEvent;
import ru.ivent.service.model.Event;

import lombok.experimental.UtilityClass;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Laughina
 */
@UtilityClass
public final class AfishaYktEventMapper {

    private static final String SOURCE = "afishaykt";
    private static final String BASE_URL = "https://afisha.ykt.ru";
    private static final String CURRENCY = "RUB";

    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+)");

    public static Event toEvent(AfishaYktEvent dto, String category) {
        AfishaYktEvent.AfishaYktSeance firstSeance = firstSeance(dto);

        return Event.builder()
                .id(SOURCE + ":" + dto.id())
                .source(SOURCE)
                .title(dto.name())
                .description(stripHtml(dto.description()))
                .imageUrl(resolvePoster(dto))
                .eventUrl(resolveEventUrl(dto, firstSeance))
                .venue(firstSeance != null ? firstSeance.companyName() : null)
                .category(resolveCategory(category, dto.category()))
                .startTime(resolveStartTime(firstSeance))
                .endTime(null)
                .minPrice(parseMinPrice(firstSeance))
                .currency(CURRENCY)
                .ageRestriction(null)
                .tags(dto.genre() != null && !dto.genre().isBlank()
                        ? List.of(dto.genre().trim()) : null)
                .cachedAt(Instant.now())
                .build();
    }

    private static AfishaYktEvent.@Nullable AfishaYktSeance firstSeance(@NotNull AfishaYktEvent dto) {
        if (dto.seances() == null || dto.seances().isEmpty()) return null;
        return dto.seances().getFirst();
    }

    private static Instant resolveStartTime(AfishaYktEvent.AfishaYktSeance seance) {
        if (seance == null || seance.dateTimeUnix() == null) return null;
        return Instant.ofEpochMilli(seance.dateTimeUnix());
    }

    private static Double parseMinPrice(AfishaYktEvent.AfishaYktSeance seance) {
        if (seance == null || seance.price() == null) return null;
        String raw = seance.price().trim();
        if (raw.isBlank() || raw.equalsIgnoreCase("не указано")) return null;
        Matcher m = PRICE_PATTERN.matcher(raw);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static @Nullable String resolvePoster(@NotNull AfishaYktEvent dto) {
        AfishaYktEvent.AfishaYktPoster p = dto.poster();
        if (p == null || !p.valid()) return null;
        if (p.h400() != null && !p.h400().isBlank()) return p.h400();
        if (p.original() != null && !p.original().isBlank()) return p.original();
        return p.h400();
    }

    private static @Nullable String resolveEventUrl(@NotNull AfishaYktEvent dto,
                                                    AfishaYktEvent.AfishaYktSeance seance) {
        if (isNotBlank(dto.buyButtonLink())) return dto.buyButtonLink();
        if (seance == null) return null;
        if (isNotBlank(seance.buyButtonLink())) return seance.buyButtonLink();
        if (isNotBlank(seance.saleUrl())) return seance.saleUrl();
        return BASE_URL + "/event/" + dto.id();
    }

    private static String resolveCategory(String queryCategory, String dtoCategory) {
        if (queryCategory != null && !queryCategory.isBlank()) {
            return queryCategory.toUpperCase();
        }
        return dtoCategory != null ? dtoCategory.toUpperCase() : null;
    }

    private static String stripHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]+>", "").trim();
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
