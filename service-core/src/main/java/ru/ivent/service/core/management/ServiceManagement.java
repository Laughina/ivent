package ru.ivent.service.core.management;

import ru.ivent.service.ApiService;
import ru.ivent.service.core.cache.ServiceCache;
import ru.ivent.service.core.repository.ServiceRepository;
import ru.ivent.service.core.scheduler.EventScheduler;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Laughina
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ServiceManagement {

    ServiceCache cache;
    ServiceRepository repository;
    EventScheduler scheduler;
    List<ApiService> services;

    private ServiceManagement(@NotNull Builder builder) {
        this.cache = new ServiceCache();
        this.repository = new ServiceRepository(cache);
        this.services = List.copyOf(builder.services);
        this.scheduler = new EventScheduler(services, cache, builder.intervalMinutes);
    }

    public void start() {
        logger.info("Starting with {} service(s), interval={}min", services.size(), scheduler.getIntervalMinutes());
        scheduler.start();
    }

    public void stop() {
        logger.info("Stopping schedulers…");
        scheduler.stop();
    }

    public void refreshNow() {
        logger.info("Manual refresh triggered");
        scheduler.runNow();
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final List<ApiService> services = new ArrayList<>();

        private int intervalMinutes = 60;

        public Builder register(ApiService service) {
            services.add(service);
            return this;
        }

        public Builder intervalMinutes(int minutes) {
            this.intervalMinutes = minutes;
            return this;
        }

        @Contract(" -> new")
        public @NotNull ServiceManagement build() {
            if (services.isEmpty()) {
                throw new IllegalStateException("At least one ApiService must be registered.");
            }
            return new ServiceManagement(this);
        }
    }
}
