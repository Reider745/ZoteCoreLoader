package cn.nukkit.network.protocol;

import com.zhekasmirnov.horizon.runtime.logger.Logger;

public class InnerCorePacket extends DataPacket{
    public static final byte NETWORK_ID = -64;

    public String name;
    public int format_id;
    public int bytes_length;
    public byte[] bytes;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        name = this.getString();
        format_id = this.getVarInt();
        bytes_length = this.getVarInt();
        bytes = this.get();
    }

    @Override
    public void encode() {
        this.reset();
        this.putString(name);
        this.putVarInt(format_id);
        this.putVarInt(bytes_length);
        this.put(bytes);
    }
}
