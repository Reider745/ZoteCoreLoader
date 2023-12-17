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
        InnerCoreServer.useNotSupport("World.setGameMode");
    }

    public static int getDifficulty() {
        return InnerCoreServer.server.getDifficulty();
    }

    public static void setDifficulty(int difficulty) {
        InnerCoreServer.server.setDifficulty(difficulty);
    }

    public static double getRainLevel() {
        Level level = getLevel();
        if(level != null)
            return level.isRaining() ? 1 : 0;
        return 0;
    }

    public static void setRainLevel(float v) {
        Level level = getLevel();
        if(level != null)
            level.setRaining(true);
    }

    public static double getLightningLevel() {
        Level level = getLevel();
        if(level != null)
            return level.isRaining() ? 8 : 15;
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
