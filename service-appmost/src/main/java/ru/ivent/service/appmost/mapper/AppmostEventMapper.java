package ru.ivent.service.appmost.mapper;

import ru.ivent.service.appmost.dto.AppmostEvent;
import ru.ivent.service.model.Event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * @author Laughina
 */
@Mapper
public interface AppmostEventMapper {

    AppmostEventMapper INSTANCE = Mappers.getMapper(AppmostEventMapper.class);

    @Mapping(target = "id",             source = "dto", qualifiedByName = "resolveId")
    @Mapping(target = "source",         constant = "appmost")
    @Mapping(target = "title",          source = "title")
    @Mapping(target = "description",    source = "description")
    @Mapping(target = "imageUrl",       source = "poster")
    @Mapping(target = "eventUrl",       source = "dto", qualifiedByName = "resolveUrl")
    @Mapping(target = "venue",          source = "dto", qualifiedByName = "resolveVenue")
    @Mapping(target = "category",       source = "dto", qualifiedByName = "resolveCategory")
    @Mapping(target = "startTime",      source = "dateStart", qualifiedByName = "toInstant")
    @Mapping(target = "endTime",        source = "dateEnd",   qualifiedByName = "toInstant")
    @Mapping(target = "minPrice",       source = "minPrice")
    @Mapping(target = "currency",       constant = "RUB")
    @Mapping(target = "ageRestriction", source = "ageRestriction")
    @Mapping(target = "tags",           source = "tags")
    @Mapping(target = "city",           ignore = true)
    @Mapping(target = "cachedAt",       expression = "java(java.time.Instant.now())")
    Event toEvent(AppmostEvent dto);

    @Named("resolveId")
    default String resolveId(@NotNull AppmostEvent dto) {
        return "appmost:" + dto.getId();
    }

    @Named("toInstant")
    default Instant toInstant(Long epochSeconds) {
        return epochSeconds != null ? Instant.ofEpochSecond(epochSeconds) : null;
    }

    @Named("resolveUrl")
    default String resolveUrl(@NotNull AppmostEvent dto) {
        String url = dto.getUrl();
        if (url == null) return null;
        return url.startsWith("http") ? url : "https://api.appmost.ru" + url;
    }

    @Named("resolveVenue")
    default String resolveVenue(@NotNull AppmostEvent dto) {
        return dto.getPlace() != null ? dto.getPlace().getTitle() : null;
    }

    @Named("resolveCategory")
    default String resolveCategory(@NotNull AppmostEvent dto) {
        if (dto.getCategory() == null) return null;
        String alias = dto.getCategory().getAlias();
        return alias != null ? alias.toUpperCase() : dto.getCategory().getTitle();
    }
}