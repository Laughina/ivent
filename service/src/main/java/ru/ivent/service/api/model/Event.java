package ru.ivent.service.api.model;

import lombok.Data;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

/**
 * @author Laughina
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Event {

    String id;
    String title;
    String description;
    String source;
    String category;
    String imageUrl;
    String eventUrl;
    String venue;
    String city;

    @Nullable
    Instant startTime;

    @Nullable
    Instant endTime;

    @Nullable
    Double minPrice;

    String currency;
    List<String> tags;
    String ageRestriction;
    Instant cachedAt;
}