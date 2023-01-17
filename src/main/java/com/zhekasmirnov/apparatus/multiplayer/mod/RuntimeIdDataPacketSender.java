package com.zhekasmirnov.apparatus.multiplayer.mod;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.util.Arrays;
import java.util.Base64;

public class RuntimeIdDataPacketSender {
    private static InnerCoreServer.RuntimeId runtimeIds;
    public static void loadClass(InnerCoreServer.RuntimeId runtimeIds) {
        RuntimeIdDataPacketSender.runtimeIds = runtimeIds;
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

    private static byte[] getRuntimeIdDataToSend(){
        return runtimeIds.getRuntimeIds();
    }
    private static void onReceivedRuntimeIdData(byte[] data){

    }
}
