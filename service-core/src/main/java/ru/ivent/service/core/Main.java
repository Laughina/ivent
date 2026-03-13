package ru.ivent.service.core;

import ru.ivent.service.model.Event;
import ru.ivent.service.appmost.AppmostService;
import ru.ivent.service.afishaykt.AfishaYktService;
import ru.ivent.service.core.management.ServiceManagement;
import ru.ivent.service.core.repository.ServiceRepository;

import java.util.List;

/**
 * @author Laughina
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        // 1. Build the management facade and register all service implementations
        ServiceManagement management = ServiceManagement.builder()
                .register(new AppmostService())
                .register(new AfishaYktService())
                .intervalMinutes(60)
                .build();

        // 2. Start the background scheduler (first fetch runs immediately)
        management.start();

        // 3. Give the initial fetch a moment to complete, then query
        Thread.sleep(5_000);

        ServiceRepository repo = management.repository();

        // All events from every source
        List<Event> allEvents = repo.findAll();
        System.out.println("Total cached events: " + allEvents.size());

        // Events from a specific source
        List<Event> appmostEvents = repo.findByService("appmost");
        System.out.println("Appmost events: " + appmostEvents.size());

        // Events by category
        List<Event> cinemaEvents = repo.findByCategory("CINEMA");
        System.out.println("Cinema events: " + cinemaEvents.size());

        // Find by composite id
        repo.findById("appmost:42")
                .ifPresent(e -> System.out.println("Found: " + e.title()));

        // Full-text search
        List<Event> searchResults = repo.search("концерт");
        System.out.println("Search 'концерт': " + searchResults.size());

        // 4. Force an immediate refresh at any time
        // management.refreshNow();

        // 5. On application shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(management::stop));
    }
}
