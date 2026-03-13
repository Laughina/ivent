package ru.ivent.service.core.cache;

import ru.ivent.service.model.Event;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Laughina
 */
@Slf4j
public class ServiceCache {

    private final Map<String, List<Event>> cache = new ConcurrentHashMap<>();

    public void put(String serviceName, List<Event> events) {
        cache.put(serviceName, Collections.unmodifiableList(events));
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

    public void invalidate(String serviceName) {
        cache.remove(serviceName);
    }

    public void invalidateAll() {
        cache.clear();
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
