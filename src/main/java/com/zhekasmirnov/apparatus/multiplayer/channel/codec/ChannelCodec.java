package com.zhekasmirnov.apparatus.multiplayer.channel.codec;

import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataPacket;
import com.zhekasmirnov.apparatus.util.Java8BackComp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ChannelCodec<T> implements DataChannel.IPacketListener {
    private final DataChannel channel;
    private final List<ICommonListener<T>> commonListeners = new ArrayList<>();

    public interface ICommonListener<T> {
        void receive(String name, T data);
    }


    public ChannelCodec(DataChannel channel) {
        this.channel = channel;
        channel.addListener(this);
    }

    protected abstract T decode(byte[] data) throws ChannelCodecException;
    protected abstract byte[] encode(T data);
    public abstract int getFormatId();

    public void send(String name, T data) {
        channel.send(new DataPacket(name, getFormatId(), encode(data)));
    }

    @SuppressWarnings("unchecked")
    public void sendUntyped(String name, Object data) {
        channel.send(new DataPacket(name, getFormatId(), encode((T) data)));
    }

    public DataChannel getChannel() {
        return channel;
    }

    public void addListener(ICommonListener<T> listener) {
        synchronized (commonListeners) {
            commonListeners.add(listener);
        }
    }

    @Override
    public void receive(DataPacket packet) {
        if (getFormatId() != packet.formatId) {
            return;
        }

        String name = packet.name;
        T data;
        try {
            data = decode(packet.data);
        } catch (ChannelCodecException err) {
            err.printStackTrace();
            return;
        }

        synchronized (commonListeners) {
            for (ICommonListener<T> listener : commonListeners) {
                listener.receive(name, data);
            }
        }
    }
}
