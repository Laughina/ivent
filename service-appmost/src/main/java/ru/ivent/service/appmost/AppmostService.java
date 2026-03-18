package ru.ivent.service.appmost;

import ru.ivent.service.ApiService;
import ru.ivent.service.appmost.dto.AppmostResponse;
import ru.ivent.service.appmost.mapper.AppmostEventMapper;
import ru.ivent.service.http.JsonHttpClient;
import ru.ivent.service.model.Event;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Laughina
 */
@Slf4j
public class AppmostService implements ApiService {

    private static final String SERVICE_NAME = "appmost";
    private static final int CITY_ID = 125;
    // private static final int COUNT = 100;

    private static final String URL_RECOMMENDED =
            "https://api.appmost.ru/v1/afisha/events/recommended?city_id=%d";
    private static final String URL_POPULAR =
            "https://api.appmost.ru/v1/afisha/events?city_id=%d&sort=popular";
    private static final String URL_SOON =
            "https://api.appmost.ru/v1/afisha/events/soon?city_id=%d";

    private final JsonHttpClient http = new JsonHttpClient();

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }

    @Override
    public List<Event> fetchEvents() {
        Map<String, Event> merged = new LinkedHashMap<>();

        fetchAndTag(String.format(URL_RECOMMENDED, CITY_ID), "recommended", merged);
        fetchAndTag(String.format(URL_POPULAR, CITY_ID), "popular", merged);
        fetchAndTag(String.format(URL_SOON, CITY_ID), "soon", merged);

        List<Event> result = new ArrayList<>(merged.values());
        logger.info("Total unique events: {}", result.size());
        return result;
    }

    private void fetchAndTag(String url, String tag, Map<String, Event> merged) {
        for (Event event : fetchEndpoint(url, tag)) {
            Event existing = merged.get(event.getId());
            if (existing == null) {
                addTag(event, tag);
                merged.put(event.getId(), event);
            } else {
                addTag(existing, tag);
            }
        }
    }

    private void addTag(@NotNull Event event, @NotNull String tag) {
        List<String> tags = event.getTags();
        if (tags == null) {
            event.setTags(new ArrayList<>(List.of(tag)));
        } else if (!tags.contains(tag)) {
            List<String> mutable = new ArrayList<>(tags);
            mutable.add(tag);
            event.setTags(mutable);
        }
    }

    private @NotNull List<Event> fetchEndpoint(String url, String label) {
        try {
            AppmostResponse response = JsonHttpClient.GSON.fromJson(http.getRaw(url), AppmostResponse.class);
            if (response == null || response.getEvents().isEmpty()) {
                logger.warn("Empty response for '{}' endpoint", label);
                return List.of();
            }

            List<Event> events = response.getEvents().stream()
                    .map(AppmostEventMapper.INSTANCE::toEvent)
                    .toList();

            logger.info("'{}' -> {} events", label, events.size());
            return events;

        } catch (Exception exception) {
            logger.error("Failed to fetch '{}' endpoint: {}", label, exception.getMessage(), exception);
            return List.of();
        }
    }
}