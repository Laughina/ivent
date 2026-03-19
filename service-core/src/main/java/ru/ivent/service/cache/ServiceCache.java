package ru.ivent.service.cache;

import ru.ivent.service.api.model.Event;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Laughina
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServiceCache {

    Map<String, List<Event>> cache = new ConcurrentHashMap<>();
    Map<String, Instant> updatedAt = new ConcurrentHashMap<>();

    public void put(String serviceName, List<Event> events) {
        cache.put(serviceName, Collections.unmodifiableList(events));
        updatedAt.put(serviceName, Instant.now());
        logger.debug("Updated '{}': {} events", serviceName, events.size());
    }

    public List<Event> get(String serviceName) {
        return cache.getOrDefault(serviceName, Collections.emptyList());
    }

    public List<Event> getAll() {
        return cache.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public Instant getUpdatedAt(String serviceName) {
        return updatedAt.get(serviceName);
    }

    public void invalidate(String serviceName) {
        cache.remove(serviceName);
        updatedAt.remove(serviceName);
    }

    public void invalidateAll() {
        cache.clear();
        updatedAt.clear();;
    }

    public boolean has(String serviceName) {
        List<Event> events = cache.get(serviceName);
        return events != null && !events.isEmpty();
    }

    @Override
    public String toString() {
        return "ServiceCache{services=" + cache.keySet() +
               ", totalEvents=" + getAll().size() + "}";
    }
}
