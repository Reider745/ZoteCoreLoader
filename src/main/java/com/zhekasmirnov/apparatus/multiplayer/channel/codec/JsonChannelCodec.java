package com.zhekasmirnov.apparatus.multiplayer.channel.codec;

import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataChannel;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonChannelCodec extends ChannelCodec<JSONObject> {
    public static final int FORMAT_ID = 1;

    public JsonChannelCodec(DataChannel channel) {
        super(channel);
    }

    @Override
    protected JSONObject decode(byte[] data) throws ChannelCodecException {
        try {
            return new JSONObject(new String(data));
        } catch (JSONException e) {
            throw new ChannelCodecException(e.getMessage(), e);
        }
    }

    @Override
    protected byte[] encode(JSONObject data) {
        return data.toString().getBytes();
    }

    @Override
    public int getFormatId() {
        return FORMAT_ID;
    }
}
