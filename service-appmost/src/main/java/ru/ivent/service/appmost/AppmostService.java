package ru.ivent.service.appmost;

import ru.ivent.service.api.AbstractApiService;
import ru.ivent.service.api.http.JsonHttpClient;
import ru.ivent.service.api.model.Event;
import ru.ivent.service.appmost.dto.AppmostResponse;
import ru.ivent.service.appmost.mapper.AppmostEventMapper;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Laughina
 */
@Slf4j
public final class AppmostService extends AbstractApiService {

    private static final String SERVICE_NAME = "AppMost";
    private static final int CITY_ID = 125;
    private static final int MAX_THREADS = 8;

    private static final String URL_RECOMMENDED =
            "https://api.appmost.ru/v1/afisha/events/recommended?city_id=" + CITY_ID;
    private static final String URL_POPULAR =
            "https://api.appmost.ru/v1/afisha/events?city_id=" + CITY_ID + "&sort=popular";
    private static final String URL_SOON =
            "https://api.appmost.ru/v1/afisha/events/soon?city_id=" + CITY_ID;

    private static final String BASE =
            "https://api.appmost.ru/v1/afisha/events?city_id=" + CITY_ID;

    private record FetchTask(String baseUrl, String category, String sectionTag, boolean useSeed) {

        @Contract("_, _, _ -> new")
        static @NotNull FetchTask category(String url, String cat, boolean seed) {
            return new FetchTask(url, cat, null, seed);
        }

        @Contract("_, _ -> new")
        static @NotNull FetchTask tagged(String url, String tag) {
            return new FetchTask(url, tag, tag, false);
        }
    }

    private static final List<FetchTask> ALL_TASKS;

    static {
        List<FetchTask> tasks = new ArrayList<>();

        tasks.add(FetchTask.tagged(URL_RECOMMENDED, "recommended"));
        tasks.add(FetchTask.tagged(URL_POPULAR,     "popular"));
        tasks.add(FetchTask.tagged(URL_SOON,        "soon"));

        tasks.add(FetchTask.category(BASE + "&type=release",                    "CINEMA",    false));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=5",  "CONCERT",   true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=9",  "PARTY",     true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=4",  "THEATRE",   true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=2",  "WORKSHOP",  true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=11", "EDUCATION", true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=21", "OUTDOOR",   true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=1",  "SPORT",     true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=8",  "CHILDREN",  true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=36", "MUSEUM",    true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=7",  "QUEST",     true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=12", "TOUR",      true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=13", "OTHER",     true));
        tasks.add(FetchTask.category(BASE + "&type=activity&afisha_type_id=29", "WORLD",     true));

        ALL_TASKS = List.copyOf(tasks);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public @NotNull List<Event> fetchEvents() {
        long start = System.currentTimeMillis();

        ConcurrentHashMap<String, Event> merged = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS,
                runnable -> new Thread(runnable, "appmost-fetch-" + runnable.hashCode()));
        try {
            List<? extends Future<?>> futures = ALL_TASKS.stream()
                    .map(task -> pool.submit(() -> fetchTaskAllPages(task, merged)))
                    .toList();

            for (Future<?> future : futures) {
                try {
                    future.get(90, TimeUnit.SECONDS);
                } catch (TimeoutException timeoutException) {
                    logger.warn("Task timed out after 90s");
                    future.cancel(true);
                } catch (Exception exception) {
                    logger.error("Task failed: {}", exception.getMessage());
                }
            }
        } finally {
            pool.shutdownNow();
        }

        List<Event> result = new ArrayList<>(merged.values());
        logger.info("Done in {}ms — {} unique events from {} tasks",
                System.currentTimeMillis() - start, result.size(), ALL_TASKS.size());
        return result;
    }

    private void fetchTaskAllPages(@NotNull FetchTask task, @NotNull ConcurrentHashMap<String, Event> merged) {
        int seed = task.useSeed() ? ThreadLocalRandom.current().nextInt(1, 1001) : 0;
        int page     = 1;
        int lastPage = 1;
        int fetched  = 0;

        do {
            String url = buildUrl(task, page, seed);
            AppmostResponse response = parseRaw(fetchRaw(url, task.category() + " p" + page));
            if (response == null) break;

            List<Event> events = toEvents(response);
            fetched += events.size();

            for (Event event : events) {
                if (task.sectionTag() == null) {
                    merged.putIfAbsent(event.getId(), event);
                } else {
                    merged.compute(event.getId(), (id, existing) -> {
                        if (existing == null) {
                            addTag(event, task.sectionTag());
                            return event;
                        } else {
                            addTag(existing, task.sectionTag());
                            return existing;
                        }
                    });
                }
            }

            lastPage = response.getLastPage();
            page++;
        } while (page <= lastPage && !Thread.currentThread().isInterrupted());
    }

    private @NotNull String buildUrl(@NotNull FetchTask task, int page, int seed) {
        StringBuilder url = new StringBuilder(task.baseUrl());
        if (task.useSeed()) url.append("&seed=").append(seed);
        url.append("&page=").append(page);
        return url.toString();
    }

    private AppmostResponse parseRaw(String raw) {
        if (raw == null) return null;
        AppmostResponse response = JsonHttpClient.GSON.fromJson(raw, AppmostResponse.class);
        if (response == null || response.getEvents().isEmpty()) return null;
        return response;
    }

    private @NotNull @Unmodifiable List<Event> toEvents(@NotNull AppmostResponse response) {
        return response.getEvents().stream()
                .map(AppmostEventMapper.INSTANCE::toEvent)
                .toList();
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
