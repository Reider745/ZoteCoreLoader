package com.zhekasmirnov.apparatus.multiplayer.channel.codec;

import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;

public class ChannelCodecFactory {
    public static Class<?> toCodecClass(Class<?> clazz) {
        if (String.class.isAssignableFrom(clazz)) {
            return String.class;
        }
        if (JSONObject.class.isAssignableFrom(clazz)) {
            return JSONObject.class;
        }
        if (Scriptable.class.isAssignableFrom(clazz)) {
            return Scriptable.class;
        }
        return clazz;
    }

    public static ChannelCodec<?> create(Class<?> clazz, DataChannel channel) {
        if (String.class.isAssignableFrom(clazz)) {
            return new StringChannelCodec(channel);
        }
        if (JSONObject.class.isAssignableFrom(clazz)) {
            return new JsonChannelCodec(channel);
        }
        if (Scriptable.class.isAssignableFrom(clazz)) {
            return new ScriptableChannelCodec(channel);
        }
        throw new IllegalArgumentException("cannot create channel codec for data class: " + clazz.getSimpleName());
    }

    public static ChannelCodec<?> create(int formatId, DataChannel channel) {
        switch (formatId) {
            case StringChannelCodec.FORMAT_ID:
                return new StringChannelCodec(channel);
            case JsonChannelCodec.FORMAT_ID:
                return new JsonChannelCodec(channel);
            case ScriptableChannelCodec.FORMAT_ID:
                return new ScriptableChannelCodec(channel);
        }
        throw new IllegalArgumentException("cannot create channel codec for format id: " + formatId);
    }
}
