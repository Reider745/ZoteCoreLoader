package com.reider745.network;

import cn.nukkit.network.protocol.LoginPacket;
import com.reider745.InnerCoreServer;

import java.util.UUID;

public class LoginPacketFix extends LoginPacket {
    @Override
    public void decode() {
        super.decode();
        
        if(InnerCoreServer.canOverridePlayerUUID())
            this.clientUUID = UUID.nameUUIDFromBytes(this.username.getBytes());
    }
}
