package ru.ivent.service.model;

import lombok.Data;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

/**
 * Модель событий, используемая во всех реализациях сервиса.
 *
 * @author Laughina
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Event {

    /**
     * Уникальный идентификатор, состоящий из префикса + исходного идентификатора
     */
    String id;

    /**
     * Название события
     */
    String title;

    /**
     * Описание события
     */
    String description;

    /**
     * Идентификатор сервиса события
     */
    String source;

    /**
     * Категория мероприятия: CINEMA, SHOW, THEATRE, SPORT, CHILDREN, PARTY, и др.
     */
    String category;

    /**
     * URL основного постера / изображения обложки
     */
    String imageUrl;

    /**
     * Прямая ссылка или URL веб-страницы, ведущей на мероприятие
     */
    String eventUrl;

    /**
     * Название места проведения
     */
    String venue;

    /**
     * Название города
     */
    String city;

    /**
     * Запланированное время начала
     */
    @Nullable
    Instant startTime;

    /**
     * Запланированное время окончания
     */
    @Nullable
    Instant endTime;

    /**
     * Минимальная цена билета
     */
    @Nullable
    Double minPrice;

    /**
     * Код валюты "RUB"
     */
    String currency;

    /**
     * Дополнительные теги или жанры
     */
    List<String> tags;

    /**
     * Возрастное ограничение, "18+", "0+"
     */
    String ageRestriction;

    /**
     * Отметка времени, когда эта запись события была получена
     */
    Instant cachedAt;
}
