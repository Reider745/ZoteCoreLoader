package cn.nukkit.network.protocol;

import android.util.Log;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.Network;
import cn.nukkit.network.SourceInterface;
import com.zhekasmirnov.apparatus.mcpe.NativeNetworking;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

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

    public static byte[] bytes_cache = null;

    public static HashMap<String, InetSocketAddress> addressHashMap = new HashMap<>();

    @Override
    public void decode() {
        name = this.getString();
        format_id = this.getVarInt();
        bytes_length = this.getVarInt();
        bytes = this.get();
        reciveLogic();
    }

    public void reciveLogic(){
        try{
            String client = address.getAddress().toString();
            addressHashMap.put(client, address);
            bytes_cache = bytes;
            NativeNetworking.onServerPacketReceived(client, name, format_id);
        }catch (Exception e){
            Logger.error(e.getMessage());
        }
    }

    @Override
    public void encode() {
        isEncoded = true;
        this.reset();
        this.putString(name);
        this.putVarInt(format_id);
        this.putVarInt(bytes_length);
        this.put(bytes);
    }

    public static byte[] getCurrentNativePacketBytes(){
        return bytes_cache;
    }
    public static void sendPacketToClient(String client, String name, int format_id, byte[] data){
        Logger.debug("sendPacketToClient "+client+" "+name);
        InnerCorePacket packet = new InnerCorePacket();
        packet.name = name;
        packet.format_id = format_id;
        packet.bytes_length = data.length;
        packet.bytes = data;
        packet.encode();

        Server.getInstance().getNetwork().send(addressHashMap.get(client), packet);
    }

    public static void sendPacketToServer(String name, int format_id, byte[] data){
        Logger.debug("sendPacketToServer "+name);
        InnerCorePacket packet = new InnerCorePacket();
        packet.name = name;
        packet.format_id = format_id;
        packet.bytes_length = data.length;
        packet.bytes = data;
        packet.encode();
        packet.reciveLogic();
    }
}
