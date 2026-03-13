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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * @author Laughina
 */
@Slf4j
public final class Main {

    private static final int PAGE_SIZE = 8;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter
            .ofPattern("d MMM, HH:mm", Locale.ROOT)
            .withZone(ZoneId.of("Asia/Yakutsk"));

    public static void main(String[] args) {
        String telegramToken = "8200045140:AAEp6hRBFl1Nlc0WANmMFZ4LEn3BwFzeBMA";
        ServiceManagement management = ServiceManagement.builder()
                .register(new AppmostService())
                .register(new AfishaYktService())
                .intervalMinutes(60)
                .build();

        management.start();

        Bot bot = new Bot.Builder()
                .enableTelegram(telegramToken)
                .build();

        ServiceRepository repo = management.getRepository();

        registerCommands(bot, repo);

        bot.start();

        logger.info("Bot started. Press Ctrl+C to stop.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            bot.stop();
            management.stop();
        }, "shutdown-hook"));
    }

    private static void registerCommands(@NotNull Bot bot, ServiceRepository repo) {
        var mgr = bot.getCommandManager();

        CommandKeyboardButton[] btnMenu   = new CommandKeyboardButton[1];
        CommandKeyboardButton[] btnList   = new CommandKeyboardButton[1];
        CommandKeyboardButton[] btnDetail = new CommandKeyboardButton[1];

        btnMenu[0] = mgr.registerKeyboardButton("menu", ctx -> {
            ctx.editMessage(buildMainMenu(ctx.chat(), btnList[0]));
            ctx.answerCallback(null);
        });

        btnList[0] = mgr.registerKeyboardButton("list", ctx -> {
            String category = ctx.argument(0);
            int page = ctx.argumentAs(1, Integer.class).orElse(0);
            showList(ctx, repo, category, page, btnList[0], btnMenu[0], btnDetail[0]);
        });

        btnDetail[0] = mgr.registerKeyboardButton("detail", ctx -> {
            String eventId = ctx.argument(0);
            String category = ctx.argument(1);
            int page = ctx.argumentAs(2, Integer.class).orElse(0);
            repo.findById(eventId).ifPresentOrElse(
                    e -> showDetail(ctx, e, category, page, btnList[0]),
                    () -> ctx.answerCallback("Событие не найдено")
            );
        });

        mgr.register(new Command("start", List.of()) {
            @Override
            public void execute(CommandContext ctx) {
                ctx.sendMessage(buildMainMenu(ctx.getMessage().getChat(), btnList[0]));
            }
        });
    }

    private static @NotNull OutMessage buildMainMenu(
            IdentityHolder chat,
            @NotNull CommandKeyboardButton btnList
    ) {
        return new OutMessage.Builder()
                .chat(chat)
                .text("""
                        👋 Добро пожаловать!

                        Я — бот, который помогает быть в курсе всех актуальных
                        мероприятий в городе Якутск 🎉

                        Выбирай интересующую категорию ниже и узнай, куда сходить уже сегодня!""")
                .keyboard(new InlineKeyboard.Builder()
                        .button("🏆 Лучшие события", btnList.asPayload("best", 0))
                        .row()
                        .button("🔥 Популярное", btnList.asPayload("popular", 0))
                        .button("⏰ Скоро", btnList.asPayload("soon", 0))
                        .row()
                        .button("👤 Профиль", "noop")
                        .button("⚙️ Настройки", "noop")
                        .row()
                        .button("💬 Поддержка", "noop")
                        .row()
                        .build())
                .build();
    }

    private static void showList(
            KeyboardContext ctx,
            ServiceRepository repo,
            String category,
            int page,
            CommandKeyboardButton btnList,
            CommandKeyboardButton btnMenu,
            CommandKeyboardButton btnDetail
    ) {
        List<Event> events = eventsForCategory(repo, category);
        int total = events.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        List<Event> pageItems = events.subList(
                page * PAGE_SIZE,
                Math.min((page + 1) * PAGE_SIZE, total)
        );

        String header = categoryLabel(category) + " в Якутске";
        String text = total == 0
                ? header + "\n\nСобытий не найдено."
                : header + "\n\nВыберите мероприятие, чтобы узнать подробности:";

        var kb = new InlineKeyboard.Builder();
        for (Event e : pageItems) {
            kb.button(e.getTitle(), btnDetail.asPayload(e.getId(), category, page)).row();
        }

        boolean hasPrev = page > 0;
        boolean hasNext = page < totalPages - 1;

        if (hasPrev) {
            kb.button("◀️ Пред", btnList.asPayload(category, page - 1));
        }
        kb.button("🏠 Назад", btnMenu.asPayload());
        if (hasNext) {
            kb.button("▶️ След", btnList.asPayload(category, page + 1));
        }
        kb.row();

        ctx.editMessage(new OutMessage.Builder()
                .chat(ctx.chat())
                .text(text)
                .keyboard(kb.build())
                .build());
        ctx.answerCallback(null);
    }

    private static void showDetail(
            KeyboardContext ctx,
            @NotNull Event event,
            String category,
            int page,
            CommandKeyboardButton btnList
    ) {
        var sb = new StringBuilder();
        sb.append("<b>")
                .append(escapeHtml(event.getTitle()))
                .append("</b>\n\n");

        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            String desc = event.getDescription();
            if (desc.length() > 400) desc = desc.substring(0, 400) + "...";
            sb.append(escapeHtml(desc))
                    .append("\n\n");
        }
        if (event.getStartTime() != null) {
            sb.append("📆 ")
                    .append(DATE_FMT.format(event.getStartTime()))
                    .append("\n");
        }
        if (event.getVenue() != null) {
            sb.append("📍 ")
                    .append(escapeHtml(event.getVenue()))
                    .append("\n");
        }
        if (event.getMinPrice() != null) {
            sb.append("💰 от ")
                    .append((int) event.getMinPrice().doubleValue())
                    .append(" ₽\n");
        }

        var kb = new InlineKeyboard.Builder();
        if (event.getEventUrl() != null) {
            kb.button("🎟 Купить билет / Подробнее", event.getEventUrl()).row();
        }
        kb.button("◀️ Назад к списку", btnList.asPayload(category, page)).row();

        ctx.editMessage(new OutMessage.Builder()
                .chat(ctx.chat())
                .text(sb.toString())
                .imageUrl(event.getImageUrl())
                .keyboard(kb.build())
                .disableLinksParsing(true)
                .build());
        ctx.answerCallback(null);
    }

    private static @NotNull @Unmodifiable List<Event> eventsForCategory(
            ServiceRepository repo,
            @NotNull String category
    ) {
        return switch (category) {
            case "best", "popular" -> repo.findAll().stream()
                    .filter(event -> event.getTitle() != null)
                    .toList();
            case "soon" -> repo.findAll().stream()
                    .filter(event -> event.getTitle() != null && event.getStartTime() != null)
                    .sorted(Comparator.comparing(Event::getStartTime))
                    .toList();
            default -> repo.findByCategory(category.toUpperCase()).stream()
                    .filter(event -> event.getTitle() != null)
                    .toList();
        };
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
            default         -> "📅 События";
        };
    }

    private static @NotNull String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}