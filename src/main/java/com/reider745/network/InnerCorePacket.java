package com.reider745.network;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.protocol.DataPacket;
import com.reider745.InnerCoreServer;
import com.reider745.api.ClientOnly;
import com.zhekasmirnov.apparatus.mcpe.NativeNetworking;

import java.util.*;

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

    @Override
    public DataPacket clone() {
        InnerCorePacket packet = (InnerCorePacket) super.clone();
        packet.name = name;
        packet.bytes = bytes;
        packet.bytes_length = bytes_length;
        packet.format_id = format_id;
        return packet;
    }

    @Override
    public void decode() {
        try {
            name = this.getString();
            format_id = this.getVarInt();
            bytes_length = this.getVarInt();
            bytes = this.get();

            if(InnerCoreServer.isDebugInnerCoreNetwork())
                System.out.println("Recived packet for native protocol "+name);

            NativeNetworking.onServerPacketReceived(player, name, format_id, bytes);
        } catch (Exception e) {
            Server.getInstance().getLogger().warning("Native protocol decoding error! player: "+(player == null), e);
            if(player != null) player.kick(e.getMessage());
        }
    }

    public static InnerCorePacket sendInfo;

    @Override
    public void encode() {
        isEncoded = true;
        this.reset();
        this.putString(name);
        this.putVarInt(format_id);
        this.putVarInt(bytes_length);
        this.put(bytes);
    }

    public static void sendPacketToClient(Player player, String name, int format_id, byte[] data) {
        InnerCorePacket packet = new InnerCorePacket();
        packet.name = name;
        packet.format_id = format_id;
        packet.bytes_length = data.length;
        packet.bytes = data;

        player.dataPacket(packet);
    }
}
