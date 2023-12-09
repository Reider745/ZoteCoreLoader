package com.zhekasmirnov.innercore.api.dimension;

import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ui.container.Container;
import com.zhekasmirnov.innercore.api.mod.ui.window.IWindow;

/**
 * Created by zheka on 14.11.2017.
 */

@Deprecated
public class Teleporter {
    public static final Teleporter OVERWORLD = new Teleporter(null);

    public CustomDimension dimension;
    public Region region;

    public Teleporter(CustomDimension dimension) {
        this.dimension = dimension;
        if (dimension != null) {
            this.region = dimension.getRegion();
        } else {
            this.region = Region.OVERWORLD;
        }
    }

    public static final int STATE_IDLE = 0;
    public static final int STATE_QUEUED = 1;
    public static final int STATE_RUNNING = 2;
    public static final int STATE_COMPLETE = 3;

    private int state = STATE_IDLE;

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public boolean enter() {
        if (NativeAPI.getDimension() != 0) {
            ICLog.i("ERROR", "Teleporter cannot call enter() in Nether or End dimensions");
            return false;
        } else {
            TeleportationHandler.enqueueTeleportation(this);
            return true;
        }
    }

    public boolean isIdle() {
        return state == STATE_IDLE || state == STATE_COMPLETE;
    }

    public boolean isWaiting() {
        return state == STATE_QUEUED;
    }

    public boolean isRunning() {
        return state == STATE_RUNNING;
    }

    private float targetX = 0, targetY = 0, targetZ = 0;

    private void lockTargetPosition(boolean lockPlayer) {
        NativeAPI.teleportTo(NativeAPI.getPlayer(), targetX, targetY, targetZ);
        NativeAPI.setImmobile(NativeAPI.getPlayer(), lockPlayer);
    }

    boolean start() {
        if (NativeAPI.getDimension() != 0) {
            ICLog.i("ERROR", "Teleporter cannot call start() in Nether or End dimensions");
            return false;
        }

        if (uiScreen != null) {
            uiContainer.openAs(uiScreen);
        }

        float[] position = new float[3];
        NativeAPI.getPosition(NativeAPI.getPlayer(), position);

        int playerX = (int) position[0], playerZ = (int) position[2];
        Region current = Region.getRegionAt(playerX, playerZ);

        targetX = playerX + (region.regionX - current.regionX) * Region.REGION_SIZE;
        targetZ = playerZ + (region.regionZ - current.regionZ) * Region.REGION_SIZE;
        targetY = 257.63f;

        DimensionRegistry.setCurrentCustomDimension(dimension);
        lockTargetPosition(true);
        loadingProgress = 0;

        if (callbacks != null) {
            callbacks.onStart(this);
        }

        return true;
    }

    private int loadingRadius = 3;
    private float loadingProgress = 0;

    boolean handle() {
        int total = (int) Math.pow(loadingRadius * 2 + 1, 2);
        int current = 0;

        int chunkX = (int) Math.floor(targetX / 16.0);
        int chunkZ = (int) Math.floor(targetZ / 16.0);

        for (int x = -loadingRadius; x <= loadingRadius; x++) {
            for (int z = -loadingRadius; z <= loadingRadius; z++) {
                if (NativeAPI.isChunkLoaded(chunkX + x, chunkZ + z)) {
                    current++;
                }
            }
        }

        ICLog.d("DEBUG", "dimension loading progress: " + current + "/" + total);

        loadingProgress = current / (float) total;
        if (callbacks != null) {
            callbacks.onHandle(this, loadingProgress);
        }

        if (uiScreen != null) {
            uiContainer.setScale("loadingProgress", loadingProgress);
        }

        return current == total;
    }

    private int findSurfaceCustom(int x, int y, int z) {
        /* calculate finding direction */
        boolean inTerrain = NativeAPI.getTile(x, y, z) > 0;
        int dir;
        if (inTerrain) {
            dir = 1;
        } else {
            dir = -1;
        }
        /* find surface */
        while (y >= 0 && (NativeAPI.getTile(x, y, z) > 0) == inTerrain) {
            y += dir;
        }
        /* correct */
        if (inTerrain) {
            y--;
        }
        /* return */
        return y;
    }

    void finish() {
        targetX = (float) Math.floor(targetX);
        targetZ = (float) Math.floor(targetZ);

        targetY = findSurfaceCustom((int) targetX, 128, (int) targetZ);
        if (targetY <= 4) {
            targetY = 256;
        }

        targetX += .5f;
        targetY += 1.65f;
        targetZ += .5f;

        if (callbacks != null) {
            callbacks.onComplete(this, targetX, targetY, targetZ);
        }

        lockTargetPosition(false);

        if (uiScreen != null) {
            uiContainer.close();
        }

        // NativeCallback.onDimensionLoaded();
    }

    public void setTargetPosition(float x, float y, float z) {
        targetX = x;
        targetY = y;
        targetZ = z;
    }

    public interface ITeleporterCallbacks {
        void onStart(Teleporter teleporter);

        void onHandle(Teleporter teleporter, float progress);

        void onComplete(Teleporter teleporter, float x, float y, float z);
    }

    private ITeleporterCallbacks callbacks;

    private Container uiContainer = new Container();
    private IWindow uiScreen;

    public void setCallbacks(ITeleporterCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public Container getUiContainer() {
        return uiContainer;
    }

    public void setUiScreen(IWindow uiScreen) {
        this.uiScreen = uiScreen;
    }
}
