package ru.ivent;

import ru.ivent.command.Command;
import ru.ivent.command.CommandContext;
import ru.ivent.command.CommandKeyboardButton;
import ru.ivent.command.KeyboardContext;
import ru.ivent.model.IdentityHolder;
import ru.ivent.model.InlineKeyboard;
import ru.ivent.model.OutMessage;
import ru.ivent.service.afishaykt.AfishaYktService;
import ru.ivent.service.appmost.AppmostService;
import ru.ivent.service.core.management.ServiceManagement;
import ru.ivent.service.core.repository.ServiceRepository;
import ru.ivent.service.model.Event;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Laughina
 */
@Slf4j
public final class Main {

    private static final int PAGE_SIZE = 8;

    private static final String FILTER_DATE  = "date";
    private static final String FILTER_PRICE = "price";
    private static final String FILTER_AGE   = "age";

    private static final String CONFIG_FILE = "config.properties";
    private static String botToken;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter
            .ofPattern("d MMM, HH:mm", Locale.ROOT)
            .withZone(ZoneId.of("Asia/Yakutsk"));

    public static void main(String[] args) {
        if (!loadTokenFromConfig()) {
            System.exit(1);
            return;
        }

        var management = ServiceManagement.builder()
                .register(new AppmostService())
                .register(new AfishaYktService())
                .intervalMinutes(60)
                .build();

        management.start();

        Bot bot = new Bot.Builder()
                .enableTelegram(botToken)
                .build();

        var repository = management.getRepository();

        registerCommands(bot, repository, management);

        bot.start();

        logger.info("Bot started.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            bot.stop();
            management.stop();
        }, "shutdown-hook"));
    }

    private static boolean loadTokenFromConfig() {
        Properties properties = new Properties();
        Path configFilePath = Paths.get(CONFIG_FILE);

        if (!Files.exists(configFilePath)) {
            logger.error("Config file not found: {}", configFilePath.toAbsolutePath());
            if (!Files.exists(configFilePath)) {
                try {
                    String template = """
                    bot.token=YOUR_BOT_TOKEN_HERE
                    """;
                    Files.writeString(configFilePath, template);
                } catch (IOException exception) {
                    logger.error("Failed to create config template: {}", exception.getMessage());
                }
            }
            return false;
        }

        try (InputStream input = Files.newInputStream(configFilePath)) {
            properties.load(input);
            logger.info("Config file loaded successfully: {}", configFilePath.toAbsolutePath());
        } catch (IOException exception) {
            logger.error("Error loading config file: {}", exception.getMessage());
            return false;
        }

        botToken = properties.getProperty("bot.token");

        if (botToken == null || botToken.isEmpty()) {
            logger.error("Bot token not found in config.properties");
            return false;
        }

        botToken = botToken.trim();

        return true;
    }

    private static void registerCommands(
            @NotNull Bot bot,
            ServiceRepository repository,
            @NotNull ServiceManagement management
    ) {
        var commandManager = bot.getCommandManager();

        CommandKeyboardButton[] btnMenu   = new CommandKeyboardButton[1];
        CommandKeyboardButton[] btnList   = new CommandKeyboardButton[1];
        CommandKeyboardButton[] btnDetail = new CommandKeyboardButton[1];
        CommandKeyboardButton[] btnToggle = new CommandKeyboardButton[1];

        btnMenu[0] = commandManager.registerKeyboardButton("menu", context -> {
            context.deleteMessage()
                    .thenCompose(v -> context.sendMessage(buildMainMenu(context.chat(), btnList[0])));
            context.answerCallback(null);
        });

        btnList[0] = commandManager.registerKeyboardButton("list", context -> {
            String category = context.argument(0);
            int    page     = context.argumentAs(1, Integer.class).orElse(0);
            String filters  = context.rawArgument(2).map(Object::toString).orElse("");
            showList(context, repository, category, page, filters, btnList[0], btnMenu[0], btnDetail[0], btnToggle[0]);
        });

        btnToggle[0] = commandManager.registerKeyboardButton("filter_toggle", context -> {
            String category   = context.argument(0);
            String filterKey  = context.argument(1);
            int    page       = context.argumentAs(2, Integer.class).orElse(0);
            String filters    = context.rawArgument(3).map(Object::toString).orElse("");
            String newFilters = toggleFilter(filters, filterKey);
            showList(context, repository, category, page, newFilters, btnList[0], btnMenu[0], btnDetail[0], btnToggle[0]);
        });

        btnDetail[0] = commandManager.registerKeyboardButton("detail", context -> {
            String eventId  = context.argument(0);
            String category = context.argument(1);
            int    page     = context.argumentAs(2, Integer.class).orElse(0);
            String filters  = context.rawArgument(3).map(Object::toString).orElse("");
            repository.findById(eventId).ifPresentOrElse(
                    event -> showDetail(context, event, category, page, filters, btnList[0]),
                    () -> context.answerCallback("Событие не найдено")
            );
        });

        commandManager.register(new Command("start", List.of()) {
            @Override
            public void execute(CommandContext context) {
                context.sendMessage(buildMainMenu(context.getMessage().getChat(), btnList[0]));
            }
        });

        commandManager.register(new Command("bot", List.of()) {
            @Override
            public void execute(CommandContext context) {
                context.sendMessage(new OutMessage.Builder()
                        .chat(context.getMessage().getChat())
                        .text(buildStatMessage(management))
                        .build());
            }
        });
    }

    private static @NotNull Set<String> parseFilters(@NotNull String filters) {
        if (filters.isBlank()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(filters.split(",")));
    }

    private static @NotNull String serializeFilters(@NotNull Set<String> filters) {
        return filters.stream().sorted().collect(Collectors.joining(","));
    }

    private static @NotNull String toggleFilter(@NotNull String filters, @NotNull String key) {
        Set<String> set = parseFilters(filters);
        if (!set.remove(key)) set.add(key);
        return serializeFilters(set);
    }

    private static @NotNull @Unmodifiable List<Event> applyFilters(
            @NotNull List<Event> events,
            @NotNull Set<String> filters
    ) {
        var stream = events.stream();

        if (filters.contains(FILTER_DATE)) {
            Instant now = Instant.now();
            stream = stream.filter(event -> event.getStartTime() == null || event.getStartTime().isAfter(now));
        }

        if (filters.contains(FILTER_AGE)) {
            stream = stream.filter(event -> {
                String age = event.getAgeRestriction();
                if (age == null || age.isBlank()) return true;
                String norm = age.replaceAll("[^0-9]", "");
                if (norm.isEmpty()) return true;
                try {
                    return Integer.parseInt(norm) < 18;
                } catch (NumberFormatException ex) {
                    return true;
                }
            });
        }

        if (filters.contains(FILTER_PRICE)) {
            stream = stream.sorted(Comparator.comparingDouble(
                    event -> event.getMinPrice() != null ? event.getMinPrice() : 0.0));
        }

        return stream.toList();
    }

    private static @NotNull OutMessage buildMainMenu(
            IdentityHolder chat,
            @NotNull CommandKeyboardButton btnList
    ) {
        return new OutMessage.Builder()
                .chat(chat)
                .text(
                        """
 
                        🤟🏻 Я помогу найти интересные мероприятия в Якутске — концерты, квесты, вечеринки, мастер-классы — всё что есть в городе, здесь и сейчас.
                        
                        Выбери категорию ниже и поехали 🚀
                        """
                )
                .keyboard(new InlineKeyboard.Builder()
                        .button("🏆 Лучшие события", btnList.asPayload("best", 0))
                        .row()
                        .button("🔥 Популярное", btnList.asPayload("popular", 0))
                        .button("⏰ Скоро", btnList.asPayload("soon", 0))
                        .row()
                        .button("🎬 Кино", btnList.asPayload("CINEMA", 0))
                        .button("🎭 Театр", btnList.asPayload("THEATRE", 0))
                        .button("🎭 Шоу", btnList.asPayload("SHOW", 0))
                        .row()
                        .button("🏆 Спорт", btnList.asPayload("SPORT", 0))
                        .button("👶 Детям", btnList.asPayload("CHILDREN", 0))
                        .button("🎉 Вечеринки", btnList.asPayload("PARTY", 0))
                        .row()
                        .button("👤 Профиль", "noop")
                        .button("⚙️ Настройки", "noop")
                        .row()
                        .buttonUrl("💬 Поддержка", "https://t.me/Albedo_14")
                        .row()
                        .build())
                .build();
    }

    private static void showList(
            KeyboardContext context,
            ServiceRepository repository,
            String category,
            int page,
            String filtersStr,
            CommandKeyboardButton btnList,
            CommandKeyboardButton btnMenu,
            CommandKeyboardButton btnDetail,
            CommandKeyboardButton btnToggle
    ) {
        Set<String> filters = parseFilters(filtersStr);

        List<Event> events = applyFilters(eventsForCategory(repository, category), filters);
        int total = events.size();

        if (total == 0) {
            context.answerCallback("⚠️ Событий не найдено");
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        List<Event> pageItems = events.subList(page * PAGE_SIZE, Math.min((page + 1) * PAGE_SIZE, total));

        String header     = categoryLabel(category) + " в Якутске";
        String filterHint = filters.isEmpty() ? "" : "\n<i>Фильтры: " + filterLabels(filters) + "</i>";
        String text       = header + filterHint + "\n\nВыберите мероприятие, чтобы узнать подробности:";

        var kb = new InlineKeyboard.Builder();

        for (Event event : pageItems) {
            kb.button(event.getTitle(), btnDetail.asPayload(event.getId(), category, page, filtersStr)).row();
        }

        boolean hasPrev = page > 0;
        boolean hasNext = page < totalPages - 1;

        if (hasPrev) {
            kb.button("◀️", btnList.asPayload(category, page - 1, filtersStr));
        }
        kb.button("🏠", btnMenu.asPayload());
        if (hasNext) {
            kb.button("▶️", btnList.asPayload(category, page + 1, filtersStr));
        }
        kb.row();

        kb.button(filterToggleLabel(FILTER_DATE,  filters), btnToggle.asPayload(category, FILTER_DATE,  page, filtersStr))
                .button(filterToggleLabel(FILTER_PRICE, filters), btnToggle.asPayload(category, FILTER_PRICE, page, filtersStr))
                .button(filterToggleLabel(FILTER_AGE,   filters), btnToggle.asPayload(category, FILTER_AGE,   page, filtersStr))
                .row();

        OutMessage message = new OutMessage.Builder()
                .chat(context.chat())
                .text(text)
                .keyboard(kb.build())
                .build();

        context.deleteMessage().thenCompose(v -> context.sendMessage(message));
        context.answerCallback(null);
    }

    private static void showDetail(
            KeyboardContext context,
            @NotNull Event event,
            String category,
            int page,
            String filters,
            CommandKeyboardButton btnList
    ) {
        var sb = new StringBuilder();
        sb.append("<b>").append(escapeHtml(event.getTitle())).append("</b>\n\n");

        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            String desc = event.getDescription();
            if (desc.length() > 400) desc = desc.substring(0, 400) + "...";
            sb.append(escapeHtml(desc)).append("\n\n");
        }
        if (event.getStartTime() != null) {
            sb.append("📆 ").append(DATE_FMT.format(event.getStartTime())).append("\n");
        }
        if (event.getVenue() != null) {
            sb.append("📍 ").append(escapeHtml(event.getVenue())).append("\n");
        }
        if (event.getMinPrice() != null) {
            sb.append("💰 от ").append((int) event.getMinPrice().doubleValue()).append(" ₽\n");
        }
        if (event.getAgeRestriction() != null && !event.getAgeRestriction().isBlank()) {
            sb.append("🔞 ").append(escapeHtml(event.getAgeRestriction())).append("\n");
        }

        var kb = new InlineKeyboard.Builder();
        if (event.getEventUrl() != null) {
            kb.buttonUrl("🎟 Купить билет / Подробнее", event.getEventUrl()).row();
        }
        kb.button("◀️", btnList.asPayload(category, page, filters)).row();

        context.editMessage(new OutMessage.Builder()
                .chat(context.chat())
                .text(sb.toString())
                .imageUrl(event.getImageUrl())
                .keyboard(kb.build())
                .disableLinksParsing(true)
                .build());
        context.answerCallback(null);
    }

    private static @NotNull String filterToggleLabel(@NotNull String key, @NotNull Set<String> active) {
        boolean on = active.contains(key);
        return switch (key) {
            case FILTER_DATE  -> (on ? "✅" : "🗓") + " По дате";
            case FILTER_PRICE -> (on ? "✅" : "📈") + " По цене";
            case FILTER_AGE   -> (on ? "✅" : "🔞") + " Скрыть 18+";
            default -> key;
        };
    }

    private static @NotNull String filterLabels(@NotNull Set<String> filters) {
        return filters.stream().sorted().map(f -> switch (f) {
            case FILTER_DATE  -> "дата";
            case FILTER_PRICE -> "цена";
            case FILTER_AGE   -> "18+";
            default -> f;
        }).collect(Collectors.joining(", "));
    }

    private static @NotNull @Unmodifiable List<Event> eventsForCategory(
            ServiceRepository repository,
            @NotNull String category
    ) {
        return switch (category) {
            case "best" -> repository.findAll().stream()
                    .filter(event -> event.getTitle() != null && hasTag(event, "recommended"))
                    .toList();
            case "popular" -> repository.findAll().stream()
                    .filter(event -> event.getTitle() != null && hasTag(event, "popular"))
                    .toList();
            case "soon" -> repository.findAll().stream()
                    .filter(event -> event.getTitle() != null && hasTag(event, "soon") && event.getStartTime() != null)
                    .sorted(Comparator.comparing(Event::getStartTime))
                    .toList();
            default -> repository.findByCategory(category.toUpperCase()).stream()
                    .filter(event -> event.getTitle() != null)
                    .toList();
        };
    }

    private static boolean hasTag(@NotNull Event event, @NotNull String tag) {
        List<String> tags = event.getTags();
        return tags != null && tags.contains(tag);
    }

    private static @NotNull String categoryLabel(@NotNull String category) {
        return switch (category) {
            case "best"     -> "🏆 Лучшие события";
            case "popular"  -> "🔥 Популярное";
            case "soon"     -> "⏰ Скоро";
            case "CINEMA"   -> "🎬 Кино";
            case "THEATRE"  -> "🎭 Театр";
            case "SHOW"     -> "🎭 Шоу";
            case "SPORT"    -> "🏆 Спорт";
            case "CHILDREN" -> "👶 Детям";
            case "PARTY"    -> "🎉 Вечеринки";
            case "CONCERT"  -> "🎵 Концерты";
            case "MUSEUM"   -> "🏛 Музеи";
            case "QUEST"    -> "🧩 Квесты и квизы";
            case "WORKSHOP" -> "🎨 Мастер-классы";
            case "OUTDOOR"  -> "🌿 Активный отдых";
            case "TOUR"     -> "🗺 Туры";
            default         -> "📅 События";
        };
    }

    private static @NotNull String buildStatMessage(@NotNull ServiceManagement management) {
        var scheduler = management.getScheduler();
        var cache = management.getCache();
        var services = management.getServices();

        Duration uptime = Duration.between(scheduler.getStartedAt(), Instant.now());
        String uptimeStr = formatDuration(uptime);

        long lastFetchEpoch = scheduler.getLastFetchAt().get();
        String lastFetchStr = lastFetchEpoch == 0 ? "ещё не было"
                : formatAgo(Duration.between(Instant.ofEpochSecond(lastFetchEpoch), Instant.now()));

        long secsUntilNext = scheduler.secondsUntilNextFetch();
        String nextFetchStr = secsUntilNext <= 0 ? "прямо сейчас"
                : formatDuration(Duration.ofSeconds(secsUntilNext));

        int totalCycles = scheduler.getFetchCycleCount().get();

        var sb = new StringBuilder();
        sb.append("⚙️ Информация о боте:\n");
        sb.append("– Версия: v1.0.2-beta.").append("\n");
        sb.append("– Окружение: beta").append("\n");
        sb.append("– Время работы: ").append(uptimeStr).append("\n");

        sb.append("\n⏳ Информация о планировщике:\n");
        sb.append("– Циклов парсинга: ").append(totalCycles).append("\n");
        sb.append("– Последний парс: ").append(lastFetchStr).append(" назад\n");
        sb.append("– Следующий парс: через ").append(nextFetchStr).append("\n");
        sb.append("– Интервал: каждые ").append(scheduler.getIntervalMinutes()).append(" мин\n");

        sb.append("\n\uD83D\uDD0E Источники данных:");
        int grandTotal = 0;
        for (var service : services) {
            String name = service.serviceName();
            int count = cache.get(name).size();
            // int errors = scheduler.getErrorCounts().getOrDefault(name, new java.util.concurrent.atomic.AtomicInteger(0)).get();
            Instant updated = cache.getUpdatedAt(name);
            String updatedStr = updated == null ? "—" : formatAgo(Duration.between(updated, Instant.now()));

            grandTotal += count;
            sb.append("\n— <b>").append(name).append("</b>\n");
            sb.append("  • Событий: ").append(count).append("\n");
            sb.append("  • Обновлено: ").append(updatedStr).append(" назад");
//            if (errors > 0) {
//                sb.append("  • Ошибок: ").append(errors);
//            }
        }

        sb.append("\n- Итого в кэше: ").append(grandTotal).append(" событий\n");

        var repository = management.getRepository();
        var byCategory = repository.findAll().stream()
                .filter(event -> event.getCategory() != null)
                .collect(Collectors.groupingBy(
                        event -> categoryLabel(event.getCategory()),
                        Collectors.counting()
                ));

        if (!byCategory.isEmpty()) {
            sb.append("\n\uD83D\uDCC1 По категориям:\n");
            byCategory.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(entry ->
                            sb.append("  • ").append(entry.getKey())
                                    .append(": ").append(entry.getValue()).append("\n"));
        }

        return sb.toString();
    }

    private static @NotNull String formatDuration(@NotNull Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (days > 0) return days + " д. " + hours + " ч " + minutes + " мин.";
        if (hours > 0) return hours + " ч. " + minutes + " мин.";
        if (minutes > 0) return minutes + " мин. " + seconds + " с.";
        return seconds + " с.";
    }

    private static @NotNull String formatAgo(@NotNull Duration duration) {
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        if (days > 0) return days + " д. " + duration.toHoursPart() + " ч.";
        if (hours > 0) return hours + " ч. " + duration.toMinutesPart() + " мин.";
        if (minutes > 0) return minutes + " мин";
        return duration.toSecondsPart() + " с.";
    }

    private static @NotNull String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
