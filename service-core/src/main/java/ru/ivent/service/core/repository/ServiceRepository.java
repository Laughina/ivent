package ru.ivent.service.core.repository;

import ru.ivent.service.core.cache.ServiceCache;
import ru.ivent.service.model.Event;

import lombok.RequiredArgsConstructor;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * @author Laughina
 */
@RequiredArgsConstructor
public class ServiceRepository {

    private final ServiceCache cache;

    public List<Event> findAll() {
        return cache.getAll();
    }

    public List<Event> findByService(String serviceName) {
        return cache.get(serviceName);
    }

    public List<Event> findByCategory(String category) {
        return cache.getAll().stream()
                .filter(event -> category.equalsIgnoreCase(event.category()))
                .toList();
    }

    public List<Event> findByServiceAndCategory(String serviceName, String category) {
        return cache.get(serviceName).stream()
                .filter(event -> category.equalsIgnoreCase(event.category()))
                .toList();
    }

    public Optional<Event> findById(String id) {
        return cache.getAll().stream()
                .filter(event -> id.equals(event.id()))
                .findFirst();
    }

    public List<Event> search(@NotNull String query) {
        String queryLowerCase = query.toLowerCase();
        return cache.getAll().stream()
                .filter(event -> event.title() != null && event.title().toLowerCase().contains(queryLowerCase))
                .toList();
    }

    public int count() {
        return cache.getAll().size();
    }

    public boolean isWarm(String serviceName) {
        return cache.has(serviceName);
    }
}
