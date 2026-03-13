package ru.ivent.http;

/**
 * @author Laughina
 */
public interface Content {

    String contentType();

    <R> R accept(ContentVisitor<R> visitor);
}
