package com.zhekasmirnov.apparatus.adapter.innercore.game;

/*import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.runtime.LevelInfo;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;*/

public class Minecraft {
    public enum GameState {
        NON_WORLD,
        HOST_WORLD,
        REMOTE_WORLD
    }

    private static GameState lastWorldState = GameState.NON_WORLD;
    private static GameState state = GameState.NON_WORLD;
    private static boolean isLeaveGamePosted = false;

    public static void leaveGame() {
        if (state != GameState.NON_WORLD) {
            lastWorldState = state;
            state = GameState.NON_WORLD;
        }
       /* if (NativeCallback.isLevelDisplayed()) {
            MainThreadQueue.localThread.enqueue(NativeAPI::leaveGame);
        } else {
            isLeaveGamePosted = true;
        }*/
    }

    // TODO: remove this filth
    public static void onLevelDisplayed() {
        if (isLeaveGamePosted) {
            isLeaveGamePosted = false;
           // MainThreadQueue.localThread.enqueue(NativeAPI::leaveGame);
        }
    }

    // TODO: remove this filth
    public static void onLevelSelected() {
        isLeaveGamePosted = false;
        lastWorldState = state = GameState.HOST_WORLD;
    }

    public static void onConnectToHost(String host, int port) {
        isLeaveGamePosted = false;
        lastWorldState = state = GameState.REMOTE_WORLD;
    }

    public static void onGameStopped(boolean isServer) {
        isLeaveGamePosted = false;
        if (isServer && state == GameState.HOST_WORLD) {
            state = GameState.NON_WORLD;
           // LevelInfo.onLeft();
        }
        if (!isServer && state == GameState.REMOTE_WORLD) {
            state = GameState.NON_WORLD;
          //  LevelInfo.onLeft();
        }
    }

    public static GameState getGameState() {
        return state;
    }

    public static GameState getLastWorldState() {
        return lastWorldState;
    }
}
