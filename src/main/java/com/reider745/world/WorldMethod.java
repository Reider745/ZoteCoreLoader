package com.reider745.world;

import cn.nukkit.level.Level;
import com.reider745.InnerCoreServer;

public class WorldMethod {
    public static long getTime() {
        return InnerCoreServer.server.getLevel(0).getTime() % Level.TIME_FULL;
    }

    public static void setTime(int time) {
        InnerCoreServer.server.getLevel(0).setTime(time);
    }

    public static int getGameMode() {
        return InnerCoreServer.server.getGamemode();
    }

    public static void setGameMode(int mode) {
    }

    public static int getDifficulty() {
        return InnerCoreServer.server.getDifficulty();
    }

    public static void setDifficulty(int difficulty) {
        InnerCoreServer.server.setDifficulty(difficulty);
    }

    public static double getRainLevel() {
        return 0;
    }

    public static void setRainLevel(float v) {
    }

    public static double getLightningLevel() {
        return 0;
    }

    public static void setLightningLevel(float v) {
    }

    public static long getSeed() {
        return InnerCoreServer.server.getLevel(0).getSeed();
    }
}
