package com.zhekasmirnov.innercore.api.dimension;

import com.zhekasmirnov.innercore.api.log.ICLog;

import java.util.ArrayList;

/**
 * Created by zheka on 14.11.2017.
 */

@Deprecated
public class TeleportationHandler {
    private static final Object teleportationLock = new Object();

    private static ArrayList<Teleporter> teleportationQueue = new ArrayList<>();

    static void enqueueTeleportation(Teleporter teleporter) {
        synchronized (teleportationLock) {
            teleporter.setState(Teleporter.STATE_QUEUED);
            teleportationQueue.add(teleporter);
        }
    }

    public static void handleTeleportation() {
        synchronized (teleportationLock) {
            if (teleportationQueue.size() > 0) {
                try {
                    Teleporter teleporter = teleportationQueue.get(0);
                    if (teleporter.getState() == Teleporter.STATE_QUEUED) {
                        if (teleporter.start()) {
                            teleporter.setState(Teleporter.STATE_RUNNING);
                        } else {
                            teleporter.setState(Teleporter.STATE_IDLE);
                        }
                    } else if (teleporter.getState() == Teleporter.STATE_RUNNING) {
                        if (teleporter.handle()) {
                            teleporter.finish();
                            teleporter.setState(Teleporter.STATE_COMPLETE);
                        }
                    } else {
                        teleporter.setState(Teleporter.STATE_IDLE);
                        teleportationQueue.remove(0);
                    }
                } catch (Throwable err) {
                    ICLog.e("ERROR", "error in teleportation handling", err);
                }
            }
        }
    }
}
