package cn.nukkit.network.protocol;

import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import lombok.ToString;

import java.util.HashMap;

@ToString
public class SetLocalPlayerAsInitializedPacket extends DataPacket {
    public static final byte NETWORK_ID = ProtocolInfo.SET_LOCAL_PLAYER_AS_INITIALIZED_PACKET;

    public long eid;
    public static HashMap<String, ConnectedClient> clients = new HashMap<>();
    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        eid = this.getUnsignedVarLong();
        ConnectedClient connectedClient = clients.get(address.getAddress().toString());
        if(connectedClient == null){
            client_player.kick("Для подключения к серверу необходим inner core 2.0");
            return;
        }
        connectedClient.playerUid = eid;
    }

    @Override
    public void encode() {
        this.putUnsignedVarLong(eid);
    }
}
