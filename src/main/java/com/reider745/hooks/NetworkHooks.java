package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.CompressionProvider;
import cn.nukkit.network.Network;
import cn.nukkit.network.protocol.BiomeDefinitionListPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.session.RakNetPlayerSession;
import cn.nukkit.utils.*;
import com.reider745.InnerCoreServer;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.ShadowGetField;
import com.reider745.network.BasePacket;
import com.reider745.network.InnerCorePacket;
import com.reider745.network.LoginPacketFix;
import com.reider745.world.BiomesMethods;
import com.zhekasmirnov.apparatus.mcpe.NativeNetworking;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import io.netty.channel.ChannelHandlerContext;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;

import java.io.ByteArrayInputStream;
import java.net.ProtocolException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Hooks(className = "cn.nukkit.network.Network")
public class NetworkHooks implements HookClass {
    private static final LinkedList<String> canSendInfo = new LinkedList<>();

    public static void close(Player player) {
        try {
            canSendInfo.remove(NativeNetworking.getId((RakNetPlayerSession) player.getNetworkSession()));
        } catch (Exception e) {
            Logger.error("Error convert", e);
        }
    }

    @ShadowGetField(className = "cn.nukkit.network.session.RakNetPlayerSession", field = "outbound")
    public static Queue<DataPacket> getOutbound(RakNetPlayerSession session){
        return null;
    }

    @ShadowGetField(className = "cn.nukkit.network.session.RakNetPlayerSession", field = "inbound")
    public static Queue<DataPacket> getInbound(RakNetPlayerSession session){
        return null;
    }

    public static void sendPacketNotPlayer(RakNetPlayerSession session, BasePacket packet){
        if (!session.getChannel().isActive()) {
            return;
        }

        if (packet.packetId() != ProtocolInfo.toNewProtocolID(ProtocolInfo.BATCH_PACKET)) {
            packet.tryEncode();
        }
        getOutbound(session).offer(packet);
    }

    // TODO: Костыль для получения RakNetPlayerSession в стоковом Nukkit-Mot
    private static final ConcurrentHashMap<Object, RakNetPlayerSession> sessions = new ConcurrentHashMap<>();

    @Inject(className = "cn.nukkit.network.session.RakNetPlayerSession")
    public static void channelRead0(RakNetPlayerSession self, ChannelHandlerContext channelHandlerContext, RakMessage msg){
        sessions.put(getInbound(self), self);
    }

    @Inject(className = "cn.nukkit.network.session.RakNetPlayerSession")
    public static void disconnect(RakNetPlayerSession self, String reason) {
        sessions.remove(getInbound(self));
    }

    @Inject(signature = "([BLjava/util/Collection;Lcn/nukkit/network/CompressionProvider;ILcn/nukkit/Player;)V", type = TypeHook.BEFORE_REPLACE)
    public static void processBatch(Network network, byte[] payload, Collection<DataPacket> packets,
                                    CompressionProvider compression, int raknetProtocol, Player player) {
        final RakNetPlayerSession session = sessions.get(packets);
        final String id = NativeNetworking.getId(session);
        if (!canSendInfo.contains(id)) {
            canSendInfo.add(id);
            sendPacketNotPlayer(session, InnerCorePacket.sendInfo);
        }

        final MainLogger log = Server.getInstance().getLogger();

        int maxSize = 3145728; // 3 * 1024 * 1024
        if (player != null && player.getSkin() == null) {
            maxSize = 6291456; // 6 * 1024 * 1024
        }

        byte[] data;
        try {
            data = compression.decompress(payload, maxSize);
        } catch (Exception e) {
            log.debug("Exception while inflating batch packet", e);
            return;
        }

        BinaryStream stream = new BinaryStream(data);
        try {
            int count = 0;
            int packetId;
            while (!stream.feof()) {
                count++;
                if (count >= 1000) {
                    throw new ProtocolException("Illegal batch with " + count + " packets");
                }
                byte[] buf = stream.getByteArray();

                ByteArrayInputStream bais = new ByteArrayInputStream(buf);

                switch (raknetProtocol) {
                    case 7:
                        packetId = bais.read();
                        break;
                    case 8:
                        packetId = bais.read();
                        bais.skip(2L);
                        break;
                    default:
                        int header = (int) VarInt.readUnsignedVarInt(bais);
                        // | Client ID | Sender ID | Packet ID |
                        // | 2 bits | 2 bits | 10 bits |
                        packetId = header & 0x3FF;
                        break;
                }

                DataPacket pk = network.getPacket(packetId, InnerCoreServer.PROTOCOL);

                if (InnerCoreServer.isDebugInnerCoreNetwork())
                    System.out.println("received batch packet id=" + packetId + ", pk: " + (pk == null));

                if (pk != null) {
                    pk.protocol = player == null ? Integer.MAX_VALUE : player.protocol;
                    pk.setBuffer(buf, buf.length - bais.available());

                    if (pk instanceof BasePacket packet) {
                        packet.session = session;
                    }

                    try {
                        if (raknetProtocol > 8) {
                            pk.decode();
                        } else { // version < 1.6
                            pk.setBuffer(buf, 3);
                            pk.decode();
                        }
                    } catch (Exception e) {
                        log.error("Unable to decode packet", e);
                        throw new IllegalStateException("Unable to decode " + pk.getClass().getSimpleName());
                    }

                    packets.add(pk);
                } else {
                    log.debug("Received unknown packet with ID: " + packetId);
                }
            }
        } catch (Exception e) {
            log.debug("Error while decoding batch packet", e);
        }
    }

    @Inject(type = TypeHook.AFTER_NOT_REPLACE)
    public static void registerPackets(Network network) {
        network.registerPacket(InnerCoreServer.PROTOCOL, InnerCorePacket.NETWORK_ID, InnerCorePacket.class);
        network.registerPacket(InnerCoreServer.PROTOCOL, LoginPacketFix.NETWORK_ID, LoginPacketFix.class);
    }

    @Inject(className = "cn.nukkit.network.protocol.BiomeDefinitionListPacket")
    public static void encode(BiomeDefinitionListPacket self) {
        BiomesMethods.encode(self);
    }
}
