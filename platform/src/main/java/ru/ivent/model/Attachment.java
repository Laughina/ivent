package ru.ivent.model;

import ru.ivent.http.EmbeddableContent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * @author Laughina
 */
public interface Attachment {

    @NotNull AttachmentType type();

    @NotNull String fileName();

    @NotNull CompletableFuture<EmbeddableContent> createContent();
}