package com.zhekasmirnov.apparatus.multiplayer.channel.codec;

import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.Scriptable;

public class ScriptableChannelCodec extends ChannelCodec<Scriptable> {
    public static final int FORMAT_ID = 3;

    public ScriptableChannelCodec(DataChannel channel) {
        super(channel);
    }

    @Override
    protected Scriptable decode(byte[] data) throws ChannelCodecException {
        try {
            return (Scriptable) NativeJSON.parse(Context.enter(), ScriptableObjectHelper.getDefaultScope(), new String(data), (context, scriptable, scriptable1, objects) -> objects[1]);
        } catch (ClassCastException e) {
            throw new ChannelCodecException(e.getMessage(), e);
        }
    }

    @Override
    protected byte[] encode(Scriptable data) {
        String result = NativeJSON.stringify(Context.enter(), ScriptableObjectHelper.getDefaultScope(), data, null, null).toString();
        return result.getBytes();
    }

    @Override
    public int getFormatId() {
        return 0;
    }
}
