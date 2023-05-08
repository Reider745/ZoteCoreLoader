package cn.nukkit.network.protocol;

import java.util.ArrayList;

public class ItemStackResponsePacket extends DataPacket {
    public static byte NETWORK_ID = ProtocolInfo.ITEM_STACK_RESPONSE_PACKET;

    public static class SlotInfo {
        public byte byte1;
        public byte byte2;
        public byte byte3;
        public int network_id;
        public String string;

        public SlotInfo(byte byte1, byte byte2, byte byte3, int network_id, String string){
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.byte3 = byte3;
            this.network_id = network_id;
            this.string = string;
        }

        public void write(ItemStackResponsePacket packet){
            packet.putByte(byte1);
            packet.putByte(byte2);
            packet.putByte(byte3);
            packet.putVarInt(network_id);
            packet.putString(string);
        }
    }

    public static class ContainerInfo {
        public byte type;
        public ArrayList<SlotInfo> slots;

        public ContainerInfo(byte type, ArrayList<SlotInfo> slots){
            this.type = type;
            this.slots = slots;
        }

        public void write(ItemStackResponsePacket packet){
            packet.putByte(type);
            packet.writeVector(slots, (v) -> v.write(packet));
        }
    }

    public static class Info {
        public byte byte1;
        public int network_id;
        public ArrayList<ContainerInfo> infos;

        public Info(byte byte1, int network_id){
            this.byte1 = byte1;
            this.network_id = network_id;
            this.infos = null;
        }

        public Info(byte byte1, int network_id, ArrayList<ContainerInfo> infos){
            this(byte1, network_id);
            this.infos = infos;
        }

        public void write(ItemStackResponsePacket packet){
            packet.putByte(byte1);
            packet.putVarInt(network_id);
            if (infos != null)
                packet.writeVector(infos, (v) -> v.write(packet));
        }
    }

    public ArrayList<Info> infos = new ArrayList<>();

    public ItemStackResponsePacket(ArrayList<Info> infos){
        this.infos = infos;
        this.encode();
    }

    interface IWrite<T> {void write(T value);};

    public <T>void writeVector(ArrayList<T> list, IWrite<T> func){
        this.putUnsignedVarInt(list.size());
        for(T v : list)
            func.write(v);
    }

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        writeVector(infos, (v) -> v.write(this));
    }
}
