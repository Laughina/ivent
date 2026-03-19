package ru.ivent.service.api;

import ru.ivent.service.api.model.Event;

import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * @author Laughina
 */
public interface ApiService {

    String getServiceName();

    @Unmodifiable
    List<Event> fetchEvents();
}
