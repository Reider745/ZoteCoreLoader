package com.reider745.network.innercore;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.DataPacket;
import com.zhekasmirnov.apparatus.multiplayer.channel.data.SocketDataChannel;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;

public class PlayerSocketSendingThread extends Thread {
    private LinkedBlockingDeque<DataPacket> list = new LinkedBlockingDeque<>();

    private final DataOutputStream outputStream;
    private final SocketDataChannel channel;

    public PlayerSocketSendingThread(Socket socket, SocketDataChannel channel) throws IOException {
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.channel = channel;

        start();
    }

    public void send(DataPacket packet){
        list.add(packet);
    }

    private boolean isClose = false;

    @Override
    public void run() {
        while (true){
            try{
                if(isClose) break;

                if(list.size() > InnerCoreServer.getMaxPacketSending()){
                    channel.close();
                    break;
                }

                DataPacket packet = list.take();

                outputStream.writeUTF(packet.name);
                outputStream.writeByte(packet.formatId);
                outputStream.writeInt(packet.data.length);
                outputStream.write(packet.data);
            }catch (Exception e){
                if(InnerCoreServer.isDeveloperMode())
                    Logger.error(ICLog.getStackTrace(e));
                channel.close();
            }
        }
    }

    public void close(){
        long start = System.currentTimeMillis();
        while ((list.size() <= 15 && !list.isEmpty()) || System.currentTimeMillis() - start <= 1000L){
            try {
                Thread.sleep(10);
            }catch (Exception ignore){}
        }

        isClose = true;

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
