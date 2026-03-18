package ru.ivent.service.core.scheduler;

import lombok.experimental.NonFinal;
import ru.ivent.service.ApiService;
import ru.ivent.service.core.cache.ServiceCache;
import ru.ivent.service.model.Event;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EventScheduler {

    List<ApiService> services;
    ServiceCache cache;

    @Getter
    int intervalMinutes;

    @Getter
    Instant startedAt = Instant.now();

    @Getter
    AtomicInteger fetchCycleCount = new AtomicInteger(0);

    @Getter
    AtomicLong lastFetchAt = new AtomicLong(0);

    @Getter
    Map<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "event-scheduler");
        thread.setDaemon(true);
        return thread;
    });

    ExecutorService fetchPool = Executors.newVirtualThreadPerTaskExecutor();

    @NonFinal
    ScheduledFuture<?> scheduledTask;

    public EventScheduler(
            @NotNull List<ApiService> services,
            ServiceCache cache,
            int intervalMinutes
    ) {
        this.services = services;
        this.cache = cache;
        this.intervalMinutes = intervalMinutes;

        services.forEach(service -> errorCounts.put(service.serviceName(), new AtomicInteger(0)));
    }

    public void start() {
        scheduledTask = scheduler.scheduleAtFixedRate(
                this::fetchAll,
                0,
                intervalMinutes,
                TimeUnit.MINUTES
        );
        logger.info("Started — interval: {} min, services: {}", intervalMinutes,
                services.stream()
                        .map(ApiService::serviceName)
                        .toList());
    }

    public void runNow() {
        fetchAll();
    }

    public void stop() {
        scheduler.shutdown();
        fetchPool.shutdown();
        try {
            if (!fetchPool.awaitTermination(30, TimeUnit.SECONDS)) {
                fetchPool.shutdownNow();
            }
        } catch (InterruptedException exception) {
            fetchPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Stopped.");
    }

    public long secondsUntilNextFetch() {
        if (scheduledTask == null) return -1;
        return scheduledTask.getDelay(TimeUnit.SECONDS);
    }

    private void fetchAll() {
        logger.info("Starting fetch cycle #{} for {} service(s)…",
                fetchCycleCount.get() + 1, services.size());

        List<CompletableFuture<Void>> futures = services.stream()
                .map(service -> CompletableFuture.runAsync(() -> fetchOne(service), fetchPool))
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .whenComplete((v, throwable) -> {
                    if (throwable != null) {
                        logger.error("Unexpected error during fetch cycle", throwable);
                    } else {
                        fetchCycleCount.incrementAndGet();
                        lastFetchAt.set(Instant.now().getEpochSecond());
                        logger.info("Fetch cycle complete. Cache: {}", cache);
                    }
                });
    }

    private void fetchOne(@NotNull ApiService service) {
        String name = service.serviceName();
        try {
            logger.debug("Fetching '{}'…", name);
            List<Event> events = service.fetchEvents();
            cache.put(name, events);
            logger.info("'{}' → {} events cached", name, events.size());
        } catch (Exception exception) {
            errorCounts.get(name).incrementAndGet();
            logger.error("Failed to fetch '{}': {}", name, exception.getMessage(), exception);
        }
    }
}
