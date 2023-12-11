package com.zhekasmirnov.apparatus.multiplayer.channel.data;

import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class DataChannel {
    private final Object sendLock = new Object();
    private final Object receiveLock = new Object();

    private boolean isClosed = false;
    private boolean isShutdown = false;
    private final List<IPacketListener> packetListeners = new ArrayList<>();
    private ICloseListener closeListener = null;
    private IBrokenChannelListener brokenChannelListener = null;

    public interface IPacketListener {
        void receive(DataPacket packet);
    }

    public interface ICloseListener {
        void onClose();
    }

    public interface IBrokenChannelListener {
        void onBroke(IOException exception);
    }

    public void send(DataPacket packet) {
        synchronized (sendLock) {
            if (isClosed || isShutdown) {
                return;
            }
            try {
                sendImpl(packet);
            } catch (IOException e) {
                channelBroke(e);
            }
        }
    }

    public DataPacket receive() {
        synchronized (receiveLock) {
            try {
                return receiveImpl();
            } catch (IOException e) {
                if (!"socket closed".equalsIgnoreCase(e.getMessage())) {
                    channelBroke(e);
                }
                return null;
            }
        }
    }

    public synchronized void close() {
        if (!isClosed) {
            isClosed = true;
            try {
                if (!isShutdown && closeListener != null) {
                    closeListener.onClose();
                }
                closeImpl();
            } catch (IOException e) {
                Logger.info(e.getMessage());
            }
        }
    }

    public synchronized void shutdownAndAwaitDisconnect() {
        if (!isShutdown && !isClosed) {
            isShutdown = true;
            if (closeListener != null) {
                closeListener.onClose();
            }
        }
    }

    private boolean isPanic = false;
    private synchronized void channelBroke(IOException exception) {
        if (isShutdown) {
            return;
        }
        // prevent recursion if listener will cause panic
        if (isPanic) {
            return;
        }
        isPanic = true;
        if (exception != null) {
            //exception.printStackTrace();
        }
        if (!isClosed) {
            if (EngineConfig.isDeveloperMode()) {
                ICLog.e("channel broke, panic", "", exception);
            }
        }
        if (brokenChannelListener != null) {
            brokenChannelListener.onBroke(exception);
        }
        isPanic = false;
        close();
    }

    public boolean isClosed() {
        return isClosed || isShutdown;
    }

    protected abstract void sendImpl(DataPacket packet) throws IOException;
    protected abstract DataPacket receiveImpl() throws IOException;
    protected abstract void closeImpl() throws IOException;
    public abstract int getProtocolId();
    public abstract String getClient();

    public void listenerLoop() {
        while (!isClosed) {
            DataPacket packet = receive();
            if (packet != null) {
                synchronized (packetListeners) {
                    for (IPacketListener listener : packetListeners) {
                        listener.receive(packet);
                    }
                }
            }
        }
    }

    public void addListener(IPacketListener listener) {
        synchronized (packetListeners) {
            packetListeners.add(listener);
        }
    }

    public void removeListener(IPacketListener listener) {
        synchronized (packetListeners) {
            packetListeners.remove(listener);
        }
    }

    public void setCloseListener(ICloseListener closeListener) {
        this.closeListener = closeListener;
    }

    public void setBrokenChannelListener(IBrokenChannelListener brokenChannelListener) {
        this.brokenChannelListener = brokenChannelListener;
    }
}
