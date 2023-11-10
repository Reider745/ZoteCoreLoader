package com.reider745.network;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.DataPacket;

public abstract class BasePacket extends DataPacket {
    public Player player;
}
