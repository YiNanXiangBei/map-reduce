package org.yinan.io;

import net.schmizz.sshj.common.LoggerFactory;
import net.schmizz.sshj.common.StreamCopier;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author yinan
 * @date 2021/5/29
 */
public class IOUtils {

    public static final Charset UTF8 = StandardCharsets.UTF_8;

    public static void closeQuietly(Closeable... closeables) {
        closeQuietly(LoggerFactory.DEFAULT, closeables);
    }

    public static ByteArrayOutputStream readFully(InputStream stream)
            throws IOException {
        return readFully(stream, LoggerFactory.DEFAULT);
    }

    public static void closeQuietly(LoggerFactory loggerFactory, Closeable... closeables) {
        for (Closeable c : closeables) {
            try {
                if (c != null)
                    c.close();
            } catch (IOException logged) {
                loggerFactory.getLogger(IOUtils.class).warn("Error closing {} - {}", c, logged);
            }
        }
    }

    public static ByteArrayOutputStream readFully(InputStream stream, LoggerFactory loggerFactory)
            throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new StreamCopier(stream, baos, loggerFactory).copy();
        return baos;
    }

}
