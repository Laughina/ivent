package ru.ivent.service;

import ru.ivent.service.model.Event;

import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * @author Laughina
 */
public interface ApiService {

    @Unmodifiable
    List<Event> fetchEvents();

    String serviceName();
}
