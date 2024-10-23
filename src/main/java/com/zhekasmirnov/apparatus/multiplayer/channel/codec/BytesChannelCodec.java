package com.zhekasmirnov.apparatus.multiplayer.channel.codec;

import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;

public class BytesChannelCodec extends ChannelCodec<byte[]> {
    public static final int FORMAT_ID = 4;

    public BytesChannelCodec(DataChannel channel) {
        super(channel);
    }

    @Override
    protected byte[] decode(byte[] data) throws ChannelCodecException {
        return data;
    }

    @Override
    protected byte[] encode(byte[] data) {
        return data;
    }

    @Override
    public int getFormatId() {
        return FORMAT_ID;
    }
}
