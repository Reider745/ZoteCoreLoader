package com.zhekasmirnov.apparatus.multiplayer.mod;

import android.util.Pair;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.GlobalBlockPalette;
import com.google.common.io.ByteStreams;
import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import io.netty.buffer.ByteBuf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

public class RuntimeIdDataPacketSender {
    public static void loadClass(InnerCoreServer.RuntimeId runtimeIds) {
        Network.getSingleton().addServerInitializationPacket("system.runtime_id_data",
                client -> RuntimeIdDataPacketSender.encode(getRuntimeIdDataToSend()),
                (String data, String meta) -> onReceivedRuntimeIdData(decode(data))
        );
    }

    private static String encode(byte[] data){
        if(data.length == 0) return "";
        return new String(Base64.getEncoder().encode(data));
    }

    private static byte[] decode(String data){
        if(data.length() == 0) return new byte[] {};
        return Base64.getDecoder().decode(data);
    }

    private static byte[] getRuntimeIdDataToSend() {
        ByteBuffer buffer = ByteBuffer.allocate(GlobalBlockPalette.getAssignedRuntimeIds().size() * 16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (Pair<Integer, Long> runtimeId : GlobalBlockPalette.getAssignedRuntimeIds()) {
            buffer.putLong(runtimeId.second);
            buffer.putInt(runtimeId.first);
            buffer.putInt(0);
        }
        return buffer.array();
    }

    private static void onReceivedRuntimeIdData(byte[] data){

    }
}
