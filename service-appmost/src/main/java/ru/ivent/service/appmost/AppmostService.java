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
    private static final int COUNT = 8;

    private static final String URL_POPULAR =
            "https://api.appmost.ru/v1/afisha/events?city_id=%d&count=%d&sort=popular";
    private static final String URL_SOON =
            "https://api.appmost.ru/v1/afisha/events/soon?city_id=%d&count=%d";
    private static final String URL_RECOMMENDED =
            "https://api.appmost.ru/v1/afisha/events/recommended?city_id=%d";

    private final JsonHttpClient http = new JsonHttpClient();

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }

    @Override
    public List<Event> fetchEvents() {
        Map<String, Event> merged = new LinkedHashMap<>();

//        fetchEndpoint(String.format(URL_POPULAR, CITY_ID, COUNT), "popular")
//                .forEach(event -> merged.putIfAbsent(event.id(), event));

        fetchEndpoint(String.format(URL_SOON, CITY_ID, COUNT), "soon")
                .forEach(event -> merged.putIfAbsent(event.id(), event));

        fetchEndpoint(String.format(URL_RECOMMENDED, CITY_ID), "recommended")
                .forEach(event -> merged.putIfAbsent(event.id(), event));

        List<Event> result = new ArrayList<>(merged.values());
        logger.info("Total unique events: {}", result.size());
        return result;
    }

    private @NotNull List<Event> fetchEndpoint(String url, String label) {
        try {
            AppmostResponse response = http.get(url, AppmostResponse.class);
            if (response == null || response.data() == null) {
                logger.warn("Empty response for '{}' endpoint", label);
                return List.of();
            }

            List<Event> events = response.data().stream()
                    .map(AppmostEventMapper::toEvent)
                    .toList();

            logger.debug("'{}' → {} events", label, events.size());
            return events;

        } catch (Exception ex) {
            logger.error("Failed to fetch '{}' endpoint: {}", label, ex.getMessage(), ex);
            return List.of();
        }
    }
}
