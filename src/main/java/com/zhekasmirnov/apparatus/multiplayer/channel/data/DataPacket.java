package com.zhekasmirnov.apparatus.multiplayer.channel.data;

import java.util.Arrays;

public class DataPacket {
    public final String name;
    public final int formatId;
    public final byte[] data;

    public DataPacket(String name, int formatId, byte[] data) {
        this.name = name;
        this.formatId = formatId;
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "name='" + name + '\'' +
                ", formatId=" + formatId +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
