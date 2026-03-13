package ru.ivent.service.afishaykt;

import ru.ivent.service.ApiService;
import ru.ivent.service.afishaykt.dto.AfishaYktEvent;
import ru.ivent.service.afishaykt.dto.AfishaYktResponse;
import ru.ivent.service.afishaykt.mapper.AfishaYktEventMapper;
import ru.ivent.service.http.JsonHttpClient;
import ru.ivent.service.model.Event;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Laughina
 */
@Slf4j
public class AfishaYktService implements ApiService {

    private static final String SERVICE_NAME = "afishaykt";

    private static final String BASE_URL = "https://afisha.ykt.ru/api/events/get";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private record Endpoint(String urlSuffix, String category) {

        @NotNull String url() {
            return BASE_URL + urlSuffix;
        }
    }

    private final JsonHttpClient http = new JsonHttpClient();

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }

    @Override
    public List<Event> fetchEvents() {
        List<Endpoint> endpoints = buildEndpoints();
        Map<String, Event> merged = new LinkedHashMap<>();

        for (Endpoint endpoint : endpoints) {
            fetchEndpoint(endpoint).forEach(e -> merged.putIfAbsent(e.id(), e));
        }

        List<Event> result = new ArrayList<>(merged.values());
        logger.info("Total unique events: {}", result.size());
        return result;
    }

    private @NotNull @Unmodifiable List<Endpoint> buildEndpoints() {
        String today = LocalDate.now().format(FMT);
        return List.of(
                new Endpoint("/" + today + "?currentCat=CINEMA&disableSoon=false", "CINEMA"),
                new Endpoint("/week?currentCat=SHOW",     "SHOW"),
                new Endpoint("/week?currentCat=THEATRE",  "THEATRE"),
                new Endpoint("/week?currentCat=SPORT",    "SPORT"),
                new Endpoint("/week?currentCat=CHILDREN", "CHILDREN"),
                new Endpoint("/week?currentCat=PARTY",    "PARTY")
        );
    }

    private @NotNull List<Event> fetchEndpoint(Endpoint endpoint) {
        try {
            AfishaYktResponse response = http.get(endpoint.url(), AfishaYktResponse.class);
            if (response == null || response.events() == null) {
                logger.warn("[AfishaYktService] Empty response for '{}' endpoint", endpoint.category());
                return List.of();
            }

            List<Event> events = new ArrayList<>();
            for (AfishaYktEvent dto : response.events()) {
                events.add(AfishaYktEventMapper.toEvent(dto, endpoint.category()));
            }

            logger.debug("[AfishaYktService] '{}' → {} events", endpoint.category(), events.size());
            return events;

        } catch (Exception ex) {
            logger.error("[AfishaYktService] Failed to fetch '{}' ({}): {}",
                    endpoint.category(), endpoint.url(), ex.getMessage(), ex);
            return List.of();
        }
    }
}
