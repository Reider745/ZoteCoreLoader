package org.innercore.icstd.component;

import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.region.BlockSource;
import org.innercore.icstd.scene.*;
import org.innercore.icstd.world.DestroyBlockContext;

public class ComponentUtils {
    public static void reportComponentCallError(Object o, Throwable err, String method) {
        String msg = o + "." + method + "() failed with exception";
        DialogHelper.reportNonFatalError(msg, err);
        ICLog.e("ComponentUtils", msg, err);
    }

    public static boolean safeCallCreateObjectFor(Object o, GameObjectHandle handle) {
        if (o instanceof CreateObjectComponent) {
            try {
                ((CreateObjectComponent) o).createObject(handle);
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "createObject");
            }
        }
        return false;
    }

    public static boolean safeCallInitFor(Object o) {
        if (o instanceof InitObjectComponent) {
            try {
                ((InitObjectComponent) o).init();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "init");
            }
        }
        return false;
    }

    public static boolean safeCallDestroyFor(Object o) {
        if (o instanceof DestroyObjectComponent) {
            try {
                ((DestroyObjectComponent) o).destroy();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "destroy");
            }
        }
        return false;
    }

    public static boolean safeCallBlockDestroyedFor(Object o, BlockSource region, BlockPos pos, DestroyBlockContext ctx) {
        if (o instanceof DestroyBlockListenerComponent) {
            try {
                ((DestroyBlockListenerComponent) o).onBlockDestroyed(region, pos, ctx);
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "onBlockDestroyed");
            }
        }
        return false;
    }

    public static boolean safeCallDiscardFor(Object o) {
        if (o instanceof DiscardObjectComponent) {
            try {
                ((DiscardObjectComponent) o).discard();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "discard");
            }
        }
        return false;
    }

    public static boolean safeCallFinalizeObjectFor(Object o) {
        if (o instanceof FinalizeObjectComponent) {
            try {
                ((FinalizeObjectComponent) o).finalizeObject();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "finalizeObject");
            }
        }
        return false;
    }

    public static boolean safeCallLoadOrUnloadFor(Object o, boolean isLoaded) {
        if (o instanceof LoadableComponent) {
            try {
                if (isLoaded)
                    ((LoadableComponent) o).load();
                else
                    ((LoadableComponent) o).unload();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, isLoaded ? "load" : "unload");
            }
        }
        return false;
    }

    public static boolean safeCallPeriodicCheckFor(Object o) {
        if (o instanceof PeriodicObjectCheckComponent) {
            try {
                ((PeriodicObjectCheckComponent) o).doPeriodicObjectCheck();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "doPeriodicObjectCheck");
            }
        }
        return false;
    }
}
