package com.zhekasmirnov.innercore.modpack.installation;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public abstract class ModPackExtractionTarget {
    public abstract OutputStream write(String name) throws IOException;

    public void writeFile(String name, File file) throws IOException {
        try (
                OutputStream outputStream = write(name);
                InputStream inputStream = new FileInputStream(file);) {
            ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
            WritableByteChannel outputChannel = Channels.newChannel(outputStream);
            ByteBuffer buffer = ByteBuffer.allocateDirect(16384);

            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.compact();
            }

            buffer.flip();
            while (buffer.hasRemaining()) {
                outputChannel.write(buffer);
            }
        }
    }
}
