package ru.ivent.service.afishaykt.mapper;

import ru.ivent.service.afishaykt.dto.AfishaYktEvent;
import ru.ivent.service.api.model.Event;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mapper
public interface AfishaYktEventMapper {

    AfishaYktEventMapper INSTANCE = Mappers.getMapper(AfishaYktEventMapper.class);

    Pattern PRICE_PATTERN = Pattern.compile("(\\d+)");
    String BASE_URL = "https://afisha.ykt.ru";

    @Mapping(target = "id",             source = "dto", qualifiedByName = "resolveId")
    @Mapping(target = "source",         constant = "afishaykt")
    @Mapping(target = "title",          source = "dto.title")
    @Mapping(target = "description",    source = "dto.description", qualifiedByName = "stripHtml")
    @Mapping(target = "imageUrl",       source = "dto", qualifiedByName = "resolvePoster")
    @Mapping(target = "eventUrl",       source = "dto", qualifiedByName = "resolveEventUrl")
    @Mapping(target = "venue",          source = "dto", qualifiedByName = "resolveVenue")
    @Mapping(target = "category",       source = "dto", qualifiedByName = "resolveCategory")
    @Mapping(target = "startTime",      source = "dto", qualifiedByName = "resolveStartTime")
    @Mapping(target = "endTime",        ignore = true)
    @Mapping(target = "minPrice",       source = "dto", qualifiedByName = "resolveMinPrice")
    @Mapping(target = "currency",       constant = "RUB")
    @Mapping(target = "ageRestriction", ignore = true)
    @Mapping(target = "tags",           source = "dto", qualifiedByName = "genreToTags")
    @Mapping(target = "city",           ignore = true)
    @Mapping(target = "cachedAt",       expression = "java(java.time.Instant.now())")
    Event toEvent(AfishaYktEvent dto, @Context String category);

    @Named("resolveId")
    default String resolveId(@NotNull AfishaYktEvent dto) {
        return "afishaykt:" + dto.getId();
    }

    @Named("stripHtml")
    default String stripHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]+>", "").trim();
    }

    @Named("resolvePoster")
    default @Nullable String resolvePoster(@NotNull AfishaYktEvent dto) {
        AfishaYktEvent.AfishaYktPoster poster = dto.getPoster();
        if (poster == null || !poster.isValid()) return null;
        if (poster.getH400() != null && !poster.getH400().isBlank()) return poster.getH400();
        if (poster.getOriginal() != null && !poster.getOriginal().isBlank()) return poster.getOriginal();
        return null;
    }

    @Named("resolveEventUrl")
    default @Nullable String resolveEventUrl(@NotNull AfishaYktEvent dto) {
        if (isNotBlank(dto.getBuyButtonLink())) return dto.getBuyButtonLink();
        AfishaYktEvent.AfishaYktSeance seance = firstSeance(dto);
        if (seance == null) return BASE_URL + "/events/view?id=" + dto.getId();
        if (isNotBlank(seance.getBuyButtonLink())) return seance.getBuyButtonLink();
        if (isNotBlank(seance.getSaleUrl())) return seance.getSaleUrl();
        return BASE_URL + "/events/view?id=" + dto.getId();
    }

    @Named("resolveVenue")
    default @Nullable String resolveVenue(AfishaYktEvent dto) {
        AfishaYktEvent.AfishaYktSeance seance = firstSeance(dto);
        return seance != null ? seance.getCompanyName() : null;
    }

    @Named("resolveCategory")
    default String resolveCategory(AfishaYktEvent dto, @Context String category) {
        if (isNotBlank(category)) return category.toUpperCase();
        return dto.getCategory() != null ? dto.getCategory().toUpperCase() : null;
    }

    @Named("resolveStartTime")
    default @Nullable Instant resolveStartTime(AfishaYktEvent dto) {
        AfishaYktEvent.AfishaYktSeance seance = firstSeance(dto);
        if (seance == null || seance.getDateTimeUnix() == null) return null;
        return Instant.ofEpochMilli(seance.getDateTimeUnix());
    }

    @Named("resolveMinPrice")
    default @Nullable Double resolveMinPrice(AfishaYktEvent dto) {
        AfishaYktEvent.AfishaYktSeance seance = firstSeance(dto);
        if (seance == null || seance.getPrice() == null) return null;
        String raw = seance.getPrice().trim();
        if (raw.isBlank() || raw.equalsIgnoreCase("не указано")) return null;
        Matcher matcher = PRICE_PATTERN.matcher(raw);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @Named("genreToTags")
    default @Nullable List<String> genreToTags(@NotNull AfishaYktEvent dto) {
        String genre = dto.getGenre();
        return (genre != null && !genre.isBlank()) ? List.of(genre.trim()) : null;
    }

    private static @Nullable AfishaYktEvent.AfishaYktSeance firstSeance(@NotNull AfishaYktEvent dto) {
        if (dto.getSeances() == null || dto.getSeances().isEmpty()) return null;
        return dto.getSeances().getFirst();
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}