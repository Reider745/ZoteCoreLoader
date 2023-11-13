package com.zhekasmirnov.apparatus.multiplayer.channel.data;

import com.zhekasmirnov.apparatus.mcpe.NativeNetworking;

import java.io.IOException;

public class NativeDataChannel extends DataChannel {
    public static final int PROTOCOL_ID = 3;

    private final NativeNetworking.NativeChannelImpl nativeImpl;

    public NativeDataChannel(NativeNetworking.NativeChannelImpl nativeImpl) {
        this.nativeImpl = nativeImpl;
    }

    @Override
    protected void sendImpl(DataPacket packet) throws IOException {
        nativeImpl.send(packet);
    }

    @Override
    protected DataPacket receiveImpl() throws IOException {
        return nativeImpl.receive();
    }

    @Override
    protected void closeImpl() throws IOException {
        nativeImpl.closeAndUnlink();
    }

    public boolean pingPong(int timeout) {
        try {
            return nativeImpl.pingPong(timeout);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getProtocolId() {
        return PROTOCOL_ID;
    }

    @Override
    public String getClient() {
        return nativeImpl.getClient();
    }
}
