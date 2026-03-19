package ru.ivent.service.api;

import ru.ivent.service.api.http.JsonHttpClient;
import ru.ivent.service.api.model.Event;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * @author Laughina
 */
@Slf4j
public abstract class AbstractApiService implements ApiService {

    protected final JsonHttpClient http = new JsonHttpClient();

    /**
     * Уникальное имя сервиса — используется как ключ в кэше и в статистике.
     */
    @Override
    public abstract String getServiceName();

    /**
     * Загружает и возвращает список событий из внешнего источника.
     * Реализация отвечает за дедупликацию и маппинг в {@link Event}.
     */
    @Override
    @Unmodifiable
    public abstract List<Event> fetchEvents();

    /**
     * Вспомогательный метод: безопасно делает GET-запрос и десериализует ответ.
     * При ошибке логирует и возвращает {@code null}.
     *
     * @param url       URL запроса
     * @param type      класс для десериализации
     * @param label     метка для логов (например, название эндпоинта)
     * @return десериализованный объект или {@code null}
     */
    protected <T> T fetchJson(@NotNull String url, @NotNull Class<T> type, @NotNull String label) {
        try {
            T result = http.get(url, type);
            if (result == null) {
                logger.warn("[{}] Empty response from '{}'", getServiceName(), label);
            }
            return result;
        } catch (Exception exception) {
            logger.error("[{}] Failed to fetch '{}': {}", getServiceName(), label, exception.getMessage(), exception);
            return null;
        }
    }

    /**
     * Вспомогательный метод: безопасно делает GET-запрос и возвращает сырой JSON.
     */
    protected String fetchRaw(@NotNull String url, @NotNull String label) {
        try {
            return http.getRaw(url);
        } catch (Exception exception) {
            logger.error("[{}] Failed to fetch raw '{}': {}", getServiceName(), label, exception.getMessage(), exception);
            return null;
        }
    }
}
