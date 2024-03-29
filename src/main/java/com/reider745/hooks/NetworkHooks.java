package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.CompressionProvider;
import cn.nukkit.network.Network;
import cn.nukkit.network.RakNetInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.Utils;
import cn.nukkit.utils.VarInt;
import com.reider745.InnerCoreServer;
import com.reider745.Main;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.network.BasePacket;
import com.reider745.network.InnerCorePacket;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.io.ByteArrayInputStream;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Hooks(className = "cn.nukkit.network.Network")
public class NetworkHooks implements HookClass {

    @Inject(signature = "([BLjava/util/Collection;Lcn/nukkit/network/CompressionProvider;ILcn/nukkit/Player;)V", type = TypeHook.BEFORE_REPLACE)
    public static void processBatch(Network network, byte[] payload, Collection<DataPacket> packets,
            CompressionProvider compression, int raknetProtocol, Player player) {
        MainLogger log = Server.getInstance().getLogger();

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

                if (InnerCoreServer.isDebugInnerCoreNetwork())
                    System.out.println("received batch packet id=" + packetId);

                // 1 - the very first package
                if (player != null && packetId == 1)
                    player.dataPacket(InnerCorePacket.sendInfo);

                DataPacket pk = network.getPacket(packetId, InnerCoreServer.PROTOCOL);

                if (pk != null) {
                    pk.protocol = player == null ? Integer.MAX_VALUE : player.protocol;
                    pk.setBuffer(buf, buf.length - bais.available());

                    if (pk instanceof BasePacket)
                        ((BasePacket) pk).player = player;

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

    @Inject
    public static void registerPackets(Network network) {
        Main.LoadingStages.registerPacket(network);
    }

    @Inject(className = "cn.nukkit.network.RakNetInterface", type = TypeHook.AFTER_NOT_REPLACE)
    public static void setName(RakNetInterface raknet, String name) {
        Object advertisementObject = ReflectHelper.getField(raknet, "advertisement");
        if (advertisementObject == null) {
            Logger.warning("No advertisement in RakNetInterface, version will not be affected.");
            return;
        }
        String advertisement = new String((byte[]) advertisementObject, StandardCharsets.UTF_8);
        String[] properties = advertisement.split(";");
        if (properties.length <= 3) {
            Logger.warning("Invalid advertisement in RakNetInterface, version will not be affected.");
            return;
        }
        properties[2] = Integer.toString(InnerCoreServer.PROTOCOL);
        properties[3] = Utils.getVersionByProtocol(InnerCoreServer.PROTOCOL);
        ReflectHelper.setField(raknet, "advertisement", String.join(";", properties).getBytes(StandardCharsets.UTF_8));
    }
}
