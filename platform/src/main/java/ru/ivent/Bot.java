package ru.ivent;

import ru.ivent.command.CommandEventHandler;
import ru.ivent.command.CommandKeyboardButtonPayloadCodec;
import ru.ivent.command.CommandManager;
import ru.ivent.command.SimpleCommandKeyboardButtonPayloadCodec;
import ru.ivent.command.SimpleCommandManager;
import ru.ivent.event.EventHandler;
import ru.ivent.event.SimpleEventDispatcher;
import ru.ivent.http.DefaultHttpClient;
import ru.ivent.http.HttpClient;
import ru.ivent.platform.Platform;
import ru.ivent.platform.PlatformType;
import ru.ivent.platform.telegram.TelegramClient;
import ru.ivent.platform.telegram.TelegramPlatform;

import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Laughina
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bot {

    Lock lifecycleLock = new ReentrantLock();

    Map<PlatformType, HttpClient> httpClients;
    Map<PlatformType, Platform> type2PlatformMap;
    List<Platform> platforms;

    @NonFinal
    List<Thread> threads;

    @Getter
    CommandManager commandManager;

    private void start0() {
        if (threads != null) return;

        httpClients.values().forEach(HttpClient::start);

        var threads = new ArrayList<Thread>(platforms.size());
        for (var platform : platforms) {
            var thread = new Thread(platform, platform.getType().getDisplayName() + " Platform");
            thread.start();
            threads.add(thread);
        }
        this.threads = threads;
    }

    public void start() {
        Lock lock;
        (lock = lifecycleLock).lock();
        try {
            start0();
        } finally {
            lock.unlock();
        }
    }

    private void stop0() {
        if (threads == null) return;

        httpClients.values().forEach(HttpClient::stop);
        threads.forEach(Thread::interrupt);
        threads = null;
    }

    public void stop() {
        Lock lock;
        (lock = lifecycleLock).lock();
        try {
            stop0();
        } finally {
            lock.unlock();
        }
    }

    public @NotNull Platform getPlatform(@NotNull PlatformType type) {
        var platform = type2PlatformMap.get(type);
        if (platform == null) {
            throw new IllegalArgumentException("Platform " + type.getDisplayName() + " not enabled.");
        }
        return platform;
    }

    public @Unmodifiable @NotNull List<@NotNull Platform> getPlatforms() {
        return Collections.unmodifiableList(platforms);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class Builder {

        @Setter
        @NonFinal
        Logger logger;

        @Setter
        @NonFinal
        JsonMapper jsonMapper;

        @Setter
        @NonFinal
        Logger telegramLogger;

        @NonFinal
        String telegramToken;

        @Setter
        @NonFinal
        Logger vkontakteLogger;

        @NonFinal
        long vkontakteDocumentOwnerId;

        @NonFinal
        String vkontakteToken;

        @NonFinal
        CommandManager commandManager;

        @NonFinal
        CommandKeyboardButtonPayloadCodec keyboardButtonPayloadCodec;

        @Setter
        @NonFinal
        boolean registerCommandEventHandler = true;

        Set<EventHandler> customEventHandlers = new HashSet<>();
        Map<PlatformType, HttpClient> type2HttpClient = new HashMap<>();

        public Builder customEventHandler(EventHandler eventHandler) {
            this.customEventHandlers.add(eventHandler);
            return this;
        }

        public Builder customCommandManager(CommandManager commandManager) {
            this.commandManager = commandManager;
            return this;
        }

        public Builder customKeyboardButtonPayloadCodec(CommandKeyboardButtonPayloadCodec codec) {
            this.keyboardButtonPayloadCodec = codec;
            return this;
        }

        public Builder enableTelegram(String token) {
            this.telegramToken = token;
            return this;
        }

        public Builder enableVkontakte(long documentOwnerId, String token) {
            this.vkontakteDocumentOwnerId = documentOwnerId;
            this.vkontakteToken = token;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            for (var platformType : PlatformType.values()) {
                this.type2HttpClient.put(platformType, httpClient);
            }
            return this;
        }

        public Builder httpClient(PlatformType platformType, HttpClient httpClient) {
            this.type2HttpClient.put(platformType, httpClient);
            return this;
        }

        public @NotNull Bot build() {
            JsonMapper jsonMapper;
            if ((jsonMapper = this.jsonMapper) == null) {
                jsonMapper = new JsonMapper();
            }

            CommandKeyboardButtonPayloadCodec keyboardButtonPayloadCodec;
            if ((keyboardButtonPayloadCodec = this.keyboardButtonPayloadCodec) == null) {
                keyboardButtonPayloadCodec = new SimpleCommandKeyboardButtonPayloadCodec(jsonMapper);
            }

            CommandManager commandManager;
            if ((commandManager = this.commandManager) == null) {
                commandManager = new SimpleCommandManager(keyboardButtonPayloadCodec);
            }

            var eventHandlers = new HashSet<>(this.customEventHandlers);
            if (registerCommandEventHandler) {
                eventHandlers.add(new CommandEventHandler(commandManager, jsonMapper));
            }

            var eventDispatcher = new SimpleEventDispatcher(logger, eventHandlers);
            var platforms = new ArrayList<Platform>();

            if (telegramToken != null) {
                HttpClient telegramHttpClient;
                if ((telegramHttpClient = this.type2HttpClient.get(PlatformType.TELEGRAM)) == null) {
                    this.type2HttpClient.put(PlatformType.TELEGRAM,
                            telegramHttpClient = new DefaultHttpClient(-1));
                }

                Logger telegramLogger;
                if ((telegramLogger = this.telegramLogger) == null) {
                    telegramLogger = LoggerFactory.getLogger("iventbot.telegram");
                }

                platforms.add(new TelegramPlatform(
                        telegramLogger,
                        new TelegramClient(telegramToken, telegramHttpClient, jsonMapper),
                        eventDispatcher
                ));
            }

            if (vkontakteToken != null) {
                HttpClient vkontakteHttpClient;
                if ((vkontakteHttpClient = this.type2HttpClient.get(PlatformType.VKONTAKTE)) == null) {
                    this.type2HttpClient.put(PlatformType.VKONTAKTE,
                            vkontakteHttpClient = new DefaultHttpClient(-1));
                }

                Logger vkontakteLogger;
                if ((vkontakteLogger = this.vkontakteLogger) == null) {
                    vkontakteLogger = LoggerFactory.getLogger("iventbot.vkontakte");
                }
            }

            var type2PlatformMap = platforms.stream()
                    .collect(Collectors.toMap(Platform::getType, Function.identity()));

            return new Bot(type2HttpClient, type2PlatformMap, platforms, commandManager);
        }
    }
}