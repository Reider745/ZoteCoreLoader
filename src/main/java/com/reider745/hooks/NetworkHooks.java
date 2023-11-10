package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.CompressionProvider;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.VarInt;
import com.reider745.Main;
import com.reider745.api.hooks.Arguments;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.AutoInject;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.network.BasePacket;

import java.io.ByteArrayInputStream;
import java.net.ProtocolException;
import java.util.Collection;

@Hooks(class_name = "cn.nukkit.network.Network")
public class NetworkHooks {

    @AutoInject(arguments = {"payload", "packets", "compression", "raknetProtocol", "player"}, static_method = false, type_hook = TypeHook.BEFORE_REPLACE)
    public static void processBatch(HookController controller){
        Arguments arguments = controller.getArguments();

        cn.nukkit.network.Network self = controller.getSelf();

        byte[] payload = arguments.arg("payload");
        Collection<DataPacket> packets = arguments.arg("packets");
        CompressionProvider compression = arguments.arg("compression");
        int raknetProtocol = arguments.arg("raknetProtocol");
        Player player = arguments.arg("player");

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
            while (!stream.feof()) {
                count++;
                if (count >= 1000) {
                    throw new ProtocolException("Illegal batch with " + count + " packets");
                }
                byte[] buf = stream.getByteArray();

                ByteArrayInputStream bais = new ByteArrayInputStream(buf);

                int packetId;
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
                        // |   2 bits  |   2 bits  |  10 bits  |
                        packetId = header & 0x3FF;
                        break;
                }

                DataPacket pk = self.getPacket(packetId);

                if (pk != null) {
                    pk.protocol = player == null ? Integer.MAX_VALUE : player.protocol;
                    pk.setBuffer(buf, buf.length - bais.available());

                    if(pk instanceof BasePacket) ((BasePacket) pk).player = player;

                    try {
                        if (raknetProtocol > 8) {
                            pk.decode();
                        }else { // version < 1.6
                            pk.setBuffer(buf, 3);
                            pk.decode();
                        }
                    } catch (Exception e) {
                       /* if (log.isTraceEnabled()) {
                            log.trace("Dumping Packet\n{}", ByteBufUtil.prettyHexDump(Unpooled.wrappedBuffer(buf)));
                        }*/
                        log.error("Unable to decode packet", e);
                        throw new IllegalStateException("Unable to decode " + pk.getClass().getSimpleName());
                    }

                    packets.add(pk);
                } else {
                    //log.debug("Received unknown packet with ID: {}", Integer.toHexString(packetId));
                }
            }
        } catch (Exception e) {
            /*if (log.isDebugEnabled()) {
                log.debug("Error whilst decoding batch packet", e);
            }*/
        }
        /*Arguments arguments = controller.getArguments();
        Player player = arguments.arg("player");
        List<DataPacket> packets = arguments.arg("packets");

        for (DataPacket packet : packets)
            if(packet instanceof BasePacket)
                ((BasePacket) packet).player = player;

        System.out.println("processPackets");*/
    }

    @AutoInject(signature = "()V", static_method = false)
    public static void registerPackets(HookController controller){
        Main.LoadingStages.registerPacket(controller.getSelf());
    }
}
