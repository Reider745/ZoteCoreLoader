package com.zhekasmirnov.apparatus.multiplayer.channel.codec;

import java.io.IOException;

public class ChannelCodecException extends IOException {
    public ChannelCodecException() {
        super();
    }

    public ChannelCodecException(String message) {
        super(message);
    }

    public ChannelCodecException(Throwable cause) {
        super(cause);
    }

    public ChannelCodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
