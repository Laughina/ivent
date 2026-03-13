package ru.ivent.platform.telegram;

import ru.ivent.http.HttpClient;
import ru.ivent.http.HttpResponse;
import ru.ivent.platform.telegram.method.TelegramAnswerCallbackQuery;
import ru.ivent.platform.telegram.method.TelegramEditMessageCaption;
import ru.ivent.platform.telegram.method.TelegramEditMessageMedia;
import ru.ivent.platform.telegram.method.TelegramEditMessageText;
import ru.ivent.platform.telegram.method.TelegramGetChat;
import ru.ivent.platform.telegram.method.TelegramGetFile;
import ru.ivent.platform.telegram.method.TelegramGetMe;
import ru.ivent.platform.telegram.method.TelegramGetUpdates;
import ru.ivent.platform.telegram.method.TelegramGetUserProfilePhotos;
import ru.ivent.platform.telegram.method.TelegramMethod;
import ru.ivent.platform.telegram.method.TelegramSendDocument;
import ru.ivent.platform.telegram.method.TelegramSendMessage;
import ru.ivent.platform.telegram.method.TelegramSendPhoto;
import ru.ivent.platform.telegram.model.ResponseOrError;

import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * @author Laughina
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class TelegramClient {

    private static final String BOT_API_URL = "https://api.telegram.org/bot";
    private static final String BOT_FILES_API_URL = "https://api.telegram.org/file/bot";

    String apiUrl;

    String fileApiUrl;

    @Getter
    HttpClient httpClient;

    @Getter
    JsonMapper jsonMapper;

    public TelegramClient(String token, HttpClient httpClient, JsonMapper jsonMapper) {
        this.apiUrl = BOT_API_URL + token;
        this.fileApiUrl = BOT_FILES_API_URL + token;
        this.httpClient = httpClient;
        this.jsonMapper = jsonMapper;
    }

    public TelegramEditMessageText editMessageText() {
        return new TelegramEditMessageText(this);
    }

    public TelegramEditMessageCaption editMessageCaption() {
        return new TelegramEditMessageCaption(this);
    }

    public TelegramEditMessageMedia editMessageMedia() {
        return new TelegramEditMessageMedia(this);
    }

    public TelegramGetMe getMe() {
        return new TelegramGetMe(this);
    }

    public TelegramGetUpdates getUpdates() {
        return new TelegramGetUpdates(this);
    }

    public TelegramSendPhoto sendPhoto() {
        return new TelegramSendPhoto(this);
    }

    public TelegramSendDocument sendDocument() {
        return new TelegramSendDocument(this);
    }

    public TelegramSendMessage sendMessage() {
        return new TelegramSendMessage(this);
    }

    public TelegramAnswerCallbackQuery answerCallbackQuery() {
        return new TelegramAnswerCallbackQuery(this);
    }

    public TelegramGetUserProfilePhotos getUserProfilePhotos() {
        return new TelegramGetUserProfilePhotos(this);
    }

    public TelegramGetFile getFile() {
        return new TelegramGetFile(this);
    }

    public TelegramGetChat getChat() {
        return new TelegramGetChat(this);
    }

    public <R> CompletableFuture<R> send(TelegramMethod<R> method) {
        var responseType = jsonMapper.getTypeFactory().constructParametricType(ResponseOrError.class, method.type());

        return httpClient.post(apiUrl + "/" + method.name(), method.params().asContent())
                .thenApply(response -> {
                    ResponseOrError<R> responseOrError;

                    try {
                        responseOrError = jsonMapper.readValue(response.getContent(), responseType);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }

                    if (!responseOrError.isOk()) {
                        throw new TelegramException("[" + responseOrError.getErrorCode() + "] " + responseOrError.getDescription());
                    }

                    return responseOrError.getResult();
                });
    }

    public CompletableFuture<HttpResponse> getFile(String path) {
        return httpClient.get(getUrlToFile(path));
    }

    public String getUrlToFile(String path) {
        return fileApiUrl + "/" + path;
    }

}
