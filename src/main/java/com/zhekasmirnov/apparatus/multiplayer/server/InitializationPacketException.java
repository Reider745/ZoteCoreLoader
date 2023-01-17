package com.zhekasmirnov.apparatus.multiplayer.server;

import java.io.IOException;

public class InitializationPacketException extends IOException {
    public InitializationPacketException() {
        super();
    }

    public InitializationPacketException(String message) {
        super(message);
    }

    public InitializationPacketException(Throwable cause) {
        super(cause);
    }

    public InitializationPacketException(String message, Throwable cause) {
        super(message, cause);
    }
}
