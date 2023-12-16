package com.reider745.world;

import cn.nukkit.level.Level;
import com.reider745.InnerCoreServer;

public class WorldMethod {

    private static Level getLevel(){
        return InnerCoreServer.server.getDefaultLevel();
    }

    public static long getTime() {
        final Level level = getLevel();
        if(level != null)
            return level.getTime() % Level.TIME_FULL;
        return 0;
    }

    public static void setTime(int time) {
        final Level level = getLevel();
        if(level != null)
            level.setTime(time);
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
        final Level level = getLevel();
        if(level != null)
            return level.getSeed();
        return 0;
    }
}
