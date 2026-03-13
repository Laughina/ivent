package ru.ivent.service;

import ru.ivent.service.model.Event;

import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * @author Laughina
 */
public interface ApiService {

    /**
     * Получить все события из API.
     *
     * @return неизменяемый список событий
     */
    @Unmodifiable
    List<Event> fetchEvents();

    /**
     * Логическое имя, идентифицирующее данную службу
     */
    String serviceName();
}
