package com.reider745.network;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.Network;
import cn.nukkit.network.protocol.BatchPacket;
import com.zhekasmirnov.apparatus.mcpe.NativeNetworking;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.SwappedByteBuf;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class InnerCorePacket extends BasePacket {
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
    public static HashMap<String, Player> playerHashMap = new HashMap<>();

    @Override
    public void decode() {
        try {
            name = this.getString();
            format_id = this.getVarInt();
            bytes_length = this.getVarInt();
            bytes = this.get();
            reciveLogic();
        } catch (Exception e) {
            Server.getInstance().getLogger().warning("Native protocol decoding error!", e);
            player.kick(e.getMessage());
        }
    }

    public static InnerCorePacket sendInfo;

    public void reciveLogic() {
        try {
            final InetSocketAddress address = this.player.getSocketAddress();
            final String client = address.getAddress().toString();

            addressHashMap.put(client, address);
            playerHashMap.put(client, player);

            bytes_cache = bytes;
            NativeNetworking.onServerPacketReceived(client, name, format_id);
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    public static void closePlayer(Player player) {
        final InetSocketAddress address = player.getSocketAddress();
        final String client = address.getAddress().toString();

        addressHashMap.remove(client);
        playerHashMap.remove(client);
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

    public static byte[] getCurrentNativePacketBytes() {
        return bytes_cache;
    }

    public static Player getPlayerForId(String client) {
        return playerHashMap.get(client);
    }

    public static void sendPacketToClient(String client, String name, int format_id, byte[] data) {
        InnerCorePacket packet = new InnerCorePacket();
        packet.name = name;
        packet.format_id = format_id;
        packet.bytes_length = data.length;
        packet.bytes = data;
        packet.encode();

        playerHashMap.get(client).dataPacket(packet);
    }

    public static void sendPacketToServer(String name, int format_id, byte[] data) {
        InnerCorePacket packet = new InnerCorePacket();
        packet.name = name;
        packet.format_id = format_id;
        packet.bytes_length = data.length;
        packet.bytes = data;
        packet.encode();
        packet.reciveLogic();
    }
}
