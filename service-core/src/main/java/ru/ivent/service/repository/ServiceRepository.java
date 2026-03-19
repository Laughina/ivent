package ru.ivent.service.repository;

import ru.ivent.service.cache.ServiceCache;
import ru.ivent.service.api.model.Event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

/**
 * @author Laughina
 */
public record ServiceRepository(ServiceCache cache) {

    public List<Event> findAll() {
        return cache.getAll();
    }

    public List<Event> findByService(String serviceName) {
        return cache.get(serviceName);
    }

    public @NotNull @Unmodifiable List<Event> findByCategory(String category) {
        return cache.getAll().stream()
                .filter(event -> category.equalsIgnoreCase(event.getCategory()))
                .toList();
    }

    public @NotNull @Unmodifiable List<Event> findByServiceAndCategory(String serviceName, String category) {
        return cache.get(serviceName).stream()
                .filter(event -> category.equalsIgnoreCase(event.getCategory()))
                .toList();
    }

    public @NotNull Optional<Event> findById(String id) {
        return cache.getAll().stream()
                .filter(event -> id.equals(event.getId()))
                .findFirst();
    }

    public @NotNull @Unmodifiable List<Event> search(@NotNull String query) {
        String queryLowerCase = query.toLowerCase();
        return cache.getAll().stream()
                .filter(event -> event.getTitle() != null
                        && event.getTitle().toLowerCase().contains(queryLowerCase))
                .toList();
    }

    public int count() {
        return cache.getAll().size();
    }

    public boolean isWarm(String serviceName) {
        return cache.has(serviceName);
    }
}
