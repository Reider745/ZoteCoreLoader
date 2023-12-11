package com.zhekasmirnov.innercore.modpack.installation;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFileExtractionTarget extends ModPackExtractionTarget implements Closeable {
    private final ZipOutputStream outputStream;

    public ZipFileExtractionTarget(File file) throws FileNotFoundException {
        outputStream = new ZipOutputStream(new FileOutputStream(file));
    }

    @Override
    public OutputStream write(String name) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        outputStream.putNextEntry(entry);
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                outputStream.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                outputStream.closeEntry();
            }
        };
    }

    public void close() throws IOException {
        outputStream.close();
    }
}
