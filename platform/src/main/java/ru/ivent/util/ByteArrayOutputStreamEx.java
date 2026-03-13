package ru.ivent.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Laughina
 */
public class ByteArrayOutputStreamEx extends ByteArrayOutputStream {

    public ByteArrayInputStream toInputStream() {
        return new ByteArrayInputStream(buf, 0, count);
    }
}
