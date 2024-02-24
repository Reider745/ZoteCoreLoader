package com.zhekasmirnov.apparatus.multiplayer.channel;

import com.zhekasmirnov.apparatus.multiplayer.channel.codec.ChannelCodec;
import com.zhekasmirnov.apparatus.multiplayer.channel.codec.ChannelCodecFactory;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelInterface {
    private final DataChannel channel;
    private final List<OnPacketReceivedListener> listeners = new ArrayList<>();
    private final Map<Class<?>, ChannelCodec<?>> codecMap = new HashMap<>();

    public interface OnPacketReceivedListener {
        void onPacketReceived(String name, Object data, Class<?> dataType);
    };

    public ChannelInterface(DataChannel channel) {
        this.channel = channel;
        getCodec(String.class);
        getCodec(JSONObject.class);
        getCodec(Scriptable.class);
    }

    @SuppressWarnings("unchecked")
    public<T> ChannelCodec<T> getCodec(final Class<T> clazz0) {
        Class<?> clazz = ChannelCodecFactory.toCodecClass(clazz0);
        return (ChannelCodec<T>) Java8BackComp.computeIfAbsent(codecMap, clazz, (Class<?> clazz1) -> {
            ChannelCodec<T> codec = (ChannelCodec<T>) ChannelCodecFactory.create(clazz, channel);
            codec.addListener((String name, T data) -> {
                for (OnPacketReceivedListener listener : listeners) {
                    listener.onPacketReceived(name, data, clazz);
                }
            });
            return codec;
        });
    }

    public<T> void send(String name, T data, Class<T> dataType) {
        getCodec(dataType).send(name, data);
    }

    public void send(String name, Object data) {
        getCodec(data.getClass()).sendUntyped(name, data);
    }

    public void addListener(OnPacketReceivedListener listener) {
        listeners.add(listener);
    }

    public void listenerLoop() {
        channel.listenerLoop();
    }

    public boolean isClosed() {
        return channel.isClosed();
    }

    public void close() {
        channel.close();
    }

    public void shutdownAndAwaitDisconnect() {
        channel.shutdownAndAwaitDisconnect();
    }

    public void shutdownAndAwaitDisconnect(int timeout) {
        shutdownAndAwaitDisconnect();
        new Thread(() -> {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ignore) { }
            close();
        }).start();
    }

    public DataChannel getChannel() {
        return channel;
    }
}
