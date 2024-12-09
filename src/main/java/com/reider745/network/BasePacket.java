package com.reider745.network;

import cn.nukkit.network.protocol.DataPacket;
import com.reider745.hooks.network.RakNetPlayerSessionHooks;

public abstract class BasePacket extends DataPacket {
    public RakNetPlayerSessionHooks session;
}
