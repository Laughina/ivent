package ru.ivent.service;

import ru.ivent.service.model.Event;

import java.util.List;

/**
 * @author Laughina
 */
public interface ApiService {

    List<Event> fetchEvents();

    String serviceName();
}
