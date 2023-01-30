package com.zhekasmirnov.apparatus.multiplayer.channel.data;

import android.util.Pair;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DirectDataChannel extends DataChannel {
    public static Pair<DirectDataChannel, DirectDataChannel> createDirectChannelPair() {
        DirectDataChannel channel1 = new DirectDataChannel();
        DirectDataChannel channel2 = new DirectDataChannel();
        channel1.setLinkedChannel(channel2);
        channel2.setLinkedChannel(channel1);
        return new Pair<>(channel1, channel2);
    }


    public static final int PROTOCOL_ID = 0;
    private static final DataPacket channelClosedPacket = new DataPacket("", 0, null);

    private final BlockingQueue<DataPacket> packets = new LinkedBlockingQueue<>();
    private DirectDataChannel linkedChannel;

    private DirectDataChannel() {

    }

    private void setLinkedChannel(DirectDataChannel linkedChannel) {
        this.linkedChannel = linkedChannel;
    }

    @Override
    protected void sendImpl(DataPacket packet) throws IOException {
        try {
            linkedChannel.packets.put(packet);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected DataPacket receiveImpl() throws IOException {
        try {
            DataPacket packet = packets.take();
            if (packet.equals(channelClosedPacket)) {
                throw new IOException("direct channel closed");
            }
            return packet;
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void closeImpl() throws IOException {
        try {
            linkedChannel.packets.put(channelClosedPacket);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public int getProtocolId() {
        return PROTOCOL_ID;
    }
}
