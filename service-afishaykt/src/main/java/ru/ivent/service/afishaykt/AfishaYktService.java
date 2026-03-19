package ru.ivent.service.afishaykt;

import ru.ivent.service.afishaykt.dto.AfishaYktResponse;
import ru.ivent.service.afishaykt.mapper.AfishaYktEventMapper;
import ru.ivent.service.api.AbstractApiService;
import ru.ivent.service.api.model.Event;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Contract;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Laughina
 */
@Slf4j
public final class AfishaYktService extends AbstractApiService {

    private static final String SERVICE_NAME = "AfishaYkt";
    private static final String BASE = "https://afisha.ykt.ru/api/events/get";
    private static final String RECOMMENDED_URL = "https://afisha.ykt.ru/api/recommended/get";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final int MAX_THREADS = 8;
    private static final int CINEMA_DAYS = 5;

    private record Endpoint(String url, String category, String sectionTag) {
        @Contract("_, _ -> new")
        static @NotNull Endpoint of(String url, String cat) {
            return new Endpoint(url, cat, null);
        }
        @Contract("_, _, _ -> new")
        static @NotNull Endpoint tagged(String url, String cat, String tag) {
            return new Endpoint(url, cat, tag);
        }
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public @NotNull List<Event> fetchEvents() {
        long start = System.currentTimeMillis();

        List<Endpoint> endpoints = buildEndpoints();
        ConcurrentHashMap<String, Event> merged = new ConcurrentHashMap<>();

        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS,
                runnable -> new Thread(runnable, "afishaykt-" + runnable.hashCode()));
        try {
            List<? extends Future<?>> futures = endpoints.stream()
                    .map(endpoint -> pool.submit(() -> fetchAndMerge(endpoint, merged)))
                    .toList();

            for (Future<?> future : futures) {
                try {
                    future.get(30, TimeUnit.SECONDS);
                } catch (TimeoutException timeoutException) {
                    logger.warn("Request timed out");
                    future.cancel(true);
                } catch (Exception exception) {
                    logger.error("Request failed: {}", exception.getMessage());
                }
            }
        } finally {
            pool.shutdownNow();
        }

        List<Event> result = new ArrayList<>(merged.values());
        logger.info("Done in {}ms — {} unique events ({} endpoints)",
                System.currentTimeMillis() - start, result.size(), endpoints.size());
        return result;
    }

    private @NotNull List<Endpoint> buildEndpoints() {
        List<Endpoint> list = new ArrayList<>();
        list.add(Endpoint.tagged(RECOMMENDED_URL, "recommended", "recommended"));

        LocalDate today = LocalDate.now();
        for (int i = 0; i < CINEMA_DAYS; i++) {
            String date = today.plusDays(i).format(DATE_FMT);
            list.add(Endpoint.of(BASE + "/" + date + "?currentCat=CINEMA&disableSoon=false", "CINEMA"));
        }

        list.add(Endpoint.of(BASE + "/week", ""));

        list.add(Endpoint.of(BASE + "/week?currentCat=SHOW",      "CONCERT"));
        list.add(Endpoint.of(BASE + "/week?currentCat=THEATRE",   "THEATRE"));
        list.add(Endpoint.of(BASE + "/week?currentCat=SPORT",     "OUTDOOR"));
        list.add(Endpoint.of(BASE + "/week?currentCat=REALSPORT", "SPORT"));
        list.add(Endpoint.of(BASE + "/week?currentCat=CHILDREN",  "CHILDREN"));
        list.add(Endpoint.of(BASE + "/week?currentCat=PARTY",     "PARTY"));
        list.add(Endpoint.of(BASE + "/week?currentCat=MEETINGS",  "MEETINGS"));
        list.add(Endpoint.of(BASE + "/week?currentCat=QUIZ",      "QUIZ"));
        list.add(Endpoint.of(BASE + "/week?currentCat=EXPO",      "EXPO"));
        list.add(Endpoint.of(BASE + "/week?currentCat=MASTER",    "WORKSHOP"));
        list.add(Endpoint.of(BASE + "/week?currentCat=TOURISM",   "TOUR"));
        list.add(Endpoint.of(BASE + "/week?currentCat=EDUCATION", "EDUCATION"));
        list.add(Endpoint.of(BASE + "/week?currentCat=QUEST",     "QUEST"));
        list.add(Endpoint.of(BASE + "/week?currentCat=FOREIGN",   "FOREIGN"));
        list.add(Endpoint.of(BASE + "/week?currentCat=OTHER",     "OTHER"));
        list.add(Endpoint.of(BASE + "/week/?currentCat=CINEMA&disableSoon=false", "CINEMA"));

        return list;
    }

    private void fetchAndMerge(@NotNull Endpoint endpoint, @NotNull ConcurrentHashMap<String, Event> merged) {
        if (Thread.currentThread().isInterrupted()) return;

        AfishaYktResponse response = fetchJson(endpoint.url(), AfishaYktResponse.class, endpoint.category());
        if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) return;

        String ctxCategory = endpoint.category().isBlank() ? null : endpoint.category();

        response.getEvents().stream()
                .map(dto -> AfishaYktEventMapper.INSTANCE.toEvent(dto, ctxCategory))
                .forEach(event -> {
                    if (endpoint.sectionTag() != null) {
                        merged.compute(event.getId(), (id, existing) -> {
                            Event target = existing != null ? existing : event;
                            addTag(target, endpoint.sectionTag());
                            return target;
                        });
                    } else {
                        merged.putIfAbsent(event.getId(), event);
                    }
                });
    }

    private void addTag(@NotNull Event event, @NotNull String tag) {
        List<String> tags = event.getTags();
        if (tags == null) {
            event.setTags(new ArrayList<>(List.of(tag)));
        } else if (!tags.contains(tag)) {
            List<String> mutable = new ArrayList<>(tags);
            mutable.add(tag);
            event.setTags(mutable);
        }
    }
}