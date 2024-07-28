package com.reider745.network;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.session.RakNetPlayerSession;

public abstract class BasePacket extends DataPacket {
    public RakNetPlayerSession session;
}
