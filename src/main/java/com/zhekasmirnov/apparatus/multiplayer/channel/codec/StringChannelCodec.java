package com.zhekasmirnov.apparatus.multiplayer.channel.codec;

import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;

public class StringChannelCodec extends ChannelCodec<String> {
    public static final int FORMAT_ID = 2;

    public StringChannelCodec(DataChannel channel) {
        super(channel);
    }

    @Override
    protected String decode(byte[] data) throws ChannelCodecException {
        return new String(data);
    }

    @Override
    protected byte[] encode(String data) {
        return data.getBytes();
    }

    @Override
    public int getFormatId() {
        return FORMAT_ID;
    }
}
