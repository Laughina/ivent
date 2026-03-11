package ru.ivent.service.model;

import lombok.Data;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

/**
 * @author Laughina
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
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
    Instant startTime;
    Instant endTime;
    Double minPrice;
    String currency;
    List<String> tags;
    String ageRestriction;
    Instant cachedAt;
}
