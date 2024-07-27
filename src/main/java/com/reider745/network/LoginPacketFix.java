package com.reider745.network;

import cn.nukkit.network.protocol.LoginPacket;

public class LoginPacketFix extends LoginPacket {
    @Override
    public void decode() {
        super.decode();

        // TODO: В случае чего можно тут подменять uuid(Чтобы клиент этим не руководил, теоритически это может быть дырой в безопасности сохранений модов)
        /*
        А вообще в идеале для совместимости, что id игрока совпадал с клиентом
         */
    }
}
