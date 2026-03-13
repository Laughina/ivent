package ru.ivent.model;

/**
 * @author Laughina
 */
public interface ImageDimensions {

    int getWidth();

    int getHeight();

    default long calculateArea() {
        return (long) getWidth() * getHeight();
    }

    default int maxSide() {
        return Math.max(getWidth(), getHeight());
    }

    default int minSide() {
        return Math.min(getWidth(), getHeight());
    }
}
