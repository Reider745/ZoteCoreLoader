package org.innercore.icstd.component;

import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.log.ICLog;
import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.region.BlockSource;
import org.innercore.icstd.scene.GameObjectHandle;
import org.innercore.icstd.world.DestroyBlockContext;

public class ComponentUtils {
    public static void reportComponentCallError(Object o, Throwable err, String method) {
        String msg = o + "." + method + "() failed with exception";
        DialogHelper.reportNonFatalError(msg, err);
        ICLog.e("ComponentUtils", msg, err);
    }

    public static boolean safeCallAddedToScene(Object o, GameObjectHandle handle) {
        if (o instanceof SceneHandleListenerComponent) {
            try {
                ((SceneHandleListenerComponent) o).onAddedToScene(handle);
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "onAddedToScene");
            }
        }
        return false;
    }

    public static boolean safeCallRemovedFromScene(Object o, GameObjectHandle handle) {
        if (o instanceof SceneHandleListenerComponent) {
            try {
                ((SceneHandleListenerComponent) o).onRemovedFromScene(handle);
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "onRemovedFromScene");
            }
        }
        return false;
    }

    public static boolean safeCallCreatedFor(Object o) {
        if (o instanceof ObjectCreateComponent) {
            try {
                ((ObjectCreateComponent) o).onCreated();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "onCreated");
            }
        }
        return false;
    }

    public static boolean safeCallDestroyFor(Object o) {
        if (o instanceof ObjectCreateComponent) {
            try {
                ((ObjectCreateComponent) o).onDestroyed();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "onDestroyed");
            }
        }
        return false;
    }

    public static boolean safeCallInitFor(Object o) {
        if (o instanceof ObjectInitComponent) {
            try {
                ((ObjectInitComponent) o).onInit();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "onInit");
            }
        }
        return false;
    }

    public static boolean safeCallDiscardFor(Object o) {
        if (o instanceof ObjectInitComponent) {
            try {
                ((ObjectInitComponent) o).onDiscard();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, "onDiscard");
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

    public static boolean safeCallLoadOrUnloadFor(Object o, boolean isLoaded) {
        if (o instanceof ObjectLoadComponent) {
            try {
                if (isLoaded)
                    ((ObjectLoadComponent) o).onLoad();
                else
                    ((ObjectLoadComponent) o).onUnload();
                return true;
            } catch (Throwable err) {
                ComponentUtils.reportComponentCallError(o, err, isLoaded ? "onLoad" : "onUnload");
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
