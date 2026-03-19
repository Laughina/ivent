package ru.ivent.service.appmost.mapper;

import ru.ivent.service.appmost.dto.AppmostEvent;
import ru.ivent.service.api.model.Event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * @author Laughina
 */
@Mapper
public interface AppmostEventMapper {

    AppmostEventMapper INSTANCE = Mappers.getMapper(AppmostEventMapper.class);

    @Mapping(target = "id",             source = "dto", qualifiedByName = "resolveId")
    @Mapping(target = "source",         constant = "appmost")
    @Mapping(target = "title",          source = "name")
    @Mapping(target = "description",    source = "dto", qualifiedByName = "resolveDescription")
    @Mapping(target = "imageUrl",       source = "poster")
    @Mapping(target = "eventUrl",       source = "link")
    @Mapping(target = "venue",          ignore = true)
    @Mapping(target = "category",       source = "dto", qualifiedByName = "resolveCategory")
    @Mapping(target = "startTime",      source = "endDate", qualifiedByName = "endDateToInstant")
    @Mapping(target = "endTime",        source = "endDate", qualifiedByName = "endDateToInstant")
    @Mapping(target = "minPrice",       source = "dto", qualifiedByName = "resolvePrice")
    @Mapping(target = "currency",       constant = "RUB")
    @Mapping(target = "ageRestriction", source = "ageRating")
    @Mapping(target = "tags",           ignore = true)
    @Mapping(target = "city",           ignore = true)
    @Mapping(target = "cachedAt",       expression = "java(java.time.Instant.now())")
    Event toEvent(AppmostEvent dto);

    @Named("resolveId")
    default String resolveId(@NotNull AppmostEvent dto) {
        return "appmost:" + dto.getId();
    }

    @Named("resolveDescription")
    default String resolveDescription(@NotNull AppmostEvent dto) {
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) return dto.getDescription();
        if (dto.getGenres() != null && !dto.getGenres().isBlank()) return dto.getGenres();
        return null;
    }

    @Named("endDateToInstant")
    default Instant endDateToInstant(String endDate) {
        if (endDate == null || endDate.isBlank()) return null;
        try {
            return LocalDate.parse(endDate)
                    .atStartOfDay(ZoneId.of("Asia/Yakutsk"))
                    .toInstant();
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    @Named("resolvePrice")
    default Double resolvePrice(@NotNull AppmostEvent dto) {
        if (dto.getMinPriceKopecks() == null) return null;
        return dto.getMinPriceKopecks() / 100.0;
    }

    @Named("resolveCategory")
    default String resolveCategory(@NotNull AppmostEvent dto) {
        if ("release".equals(dto.getType())) return "CINEMA";

        String slug = dto.getAfishaTypeSlug();
        if (slug != null && !slug.isBlank()) {
            return switch (slug) {
                case "kino", "cinema"         -> "CINEMA";
                case "teatr", "theatre"       -> "THEATRE";
                case "shou", "show"           -> "SHOW";
                case "sport"                  -> "SPORT";
                case "dlia-detei", "children" -> "CHILDREN";
                case "vecerinki", "party"     -> "PARTY";
                case "koncerty"               -> "CONCERT";
                case "muzei"                  -> "MUSEUM";
                case "master-klassy"          -> "WORKSHOP";
                case "kvizy-i-kvesty"         -> "QUEST";
                case "aktivnyi-otdyx"         -> "OUTDOOR";
                case "tours"                  -> "TOUR";
                case "obuchenie"              -> "EDUCATION";
                case "mirovaya-scena"         -> "WORLD";
                default                       -> slug.toUpperCase();
            };
        }

        String type = dto.getAfishaType();
        if (type != null) {
            return switch (type) {
                case "Кино"            -> "CINEMA";
                case "Театр"           -> "THEATRE";
                case "Шоу"             -> "SHOW";
                case "Спорт"           -> "SPORT";
                case "Для детей"       -> "CHILDREN";
                case "Вечеринки"       -> "PARTY";
                case "Концерты"        -> "CONCERT";
                case "Музеи"           -> "MUSEUM";
                case "Мастер-классы"   -> "WORKSHOP";
                case "Квизы и квесты"  -> "QUEST";
                case "Активный отдых"  -> "OUTDOOR";
                case "Туры"            -> "TOUR";
                case "Обучение"        -> "EDUCATION";
                case "Мировая сцена"   -> "WORLD";
                default                -> type.toUpperCase();
            };
        }

        return null;
    }
}
