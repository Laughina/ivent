package ru.ivent.http;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Laughina
 */
public interface EmbeddableContent extends Content {

    ReadableByteChannel open() throws IOException;
}
