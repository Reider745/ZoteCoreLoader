package com.zhekasmirnov.apparatus.adapter.innercore.game;

import com.zhekasmirnov.innercore.api.runtime.LevelInfo;

public class Minecraft {
    public enum GameState {
        NON_WORLD,
        HOST_WORLD,
        REMOTE_WORLD
    }

    private static GameState lastWorldState = GameState.NON_WORLD;
    private static GameState state = GameState.NON_WORLD;
    private static boolean isLeaveGamePosted = false;

    @Deprecated(since = "Zote")
    public static void leaveGame() {
        if (state != GameState.NON_WORLD) {
            lastWorldState = state;
            state = GameState.NON_WORLD;
        }
    }

    @Deprecated(since = "Zote")
    public static void onLevelDisplayed() {
        if (isLeaveGamePosted) {
            isLeaveGamePosted = false;
        }
    }

    public static void onLevelSelected() {
        isLeaveGamePosted = false;
        lastWorldState = state = GameState.HOST_WORLD;
    }

    @Deprecated(since = "Zote")
    public static void onConnectToHost(String host, int port) {
        isLeaveGamePosted = false;
        lastWorldState = state = GameState.REMOTE_WORLD;
    }

    public static void onGameStopped(boolean isServer) {
        isLeaveGamePosted = false;
        if (isServer && state == GameState.HOST_WORLD) {
            state = GameState.NON_WORLD;
            LevelInfo.onLeft();
        }
        if (!isServer && state == GameState.REMOTE_WORLD) {
            state = GameState.NON_WORLD;
            LevelInfo.onLeft();
        }
    }

    public static GameState getGameState() {
        return state;
    }

    public static GameState getLastWorldState() {
        return lastWorldState;
    }
}
