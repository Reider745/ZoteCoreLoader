package com.reider745.hooks.network;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.CompressionProvider;
import cn.nukkit.network.Network;
import cn.nukkit.network.RakNetInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.session.RakNetPlayerSession;
import cn.nukkit.utils.*;
import com.reider745.InnerCoreServer;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.network.BasePacket;
import com.reider745.network.InnerCorePacket;
import com.reider745.network.LoginPacketFix;
import io.netty.channel.Channel;
import org.cloudburstmc.netty.channel.raknet.RakChildChannel;

import java.io.ByteArrayInputStream;
import java.net.ProtocolException;
import java.util.*;

@Hooks(className = "cn.nukkit.network.Network")
public class NetworkHooks implements HookClass {
    public static void processBatch(Network network, byte[] payload, Collection<DataPacket> packets,
                                    CompressionProvider compression, int raknetProtocol, Player player, RakNetPlayerSessionHooks session) {
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
        network.registerPacket(InnerCoreServer.PROTOCOL, InnerCorePacket.NETWORK_ID, InnerCorePacket.class, InnerCorePacket::new);
        network.registerPacket(InnerCoreServer.PROTOCOL, LoginPacketFix.NETWORK_ID, LoginPacketFix.class, LoginPacketFix::new);
    }

    @Inject(className = "cn.nukkit.network.RakNetInterface$2", type = TypeHook.BEFORE_REPLACE)
    public static void initChannel(Object self, Channel channel) {
        final RakNetInterface rakNetInterface = ReflectHelper.getField(self, "this$0");

        RakNetPlayerSessionHooks nukkitSession = new RakNetPlayerSessionHooks(rakNetInterface, (RakChildChannel) channel);
        channel.pipeline().addLast("nukkit-handler", nukkitSession);
        ReflectHelper.<Queue<RakNetPlayerSession>>getField(rakNetInterface, "sessionCreationQueue").offer(nukkitSession);
    }
}