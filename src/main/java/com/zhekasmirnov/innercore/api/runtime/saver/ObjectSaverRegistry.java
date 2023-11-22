package com.zhekasmirnov.innercore.api.runtime.saver;

import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.container.Container;
import com.zhekasmirnov.innercore.api.runtime.saver.world.WorldDataSaver;
import org.mozilla.javascript.*;

import java.util.HashMap;

/**
 * Created by zheka on 19.08.2017.
 */

public class ObjectSaverRegistry {
    public static final String PROPERTY_IGNORE_SAVE = "_json_ignore";
    public static final String PROPERTY_SAVER_ID = "_json_saver_id";

    private static ScriptableObject scope;

    private static HashMap<Integer, ObjectSaver> saverMap = new HashMap<>();
    private static HashMap<Integer, ObjectSaver> saverByObjectHash = new HashMap<>();
    private static HashMap<Integer, String> saverNameById = new HashMap<>();

    static {
        scope = Context.enter().initStandardObjects();

        Container.initSaverId();
        NativeItemInstanceExtra.initSaverId();
    }



    public static int registerSaver(String name, ObjectSaver saver) {
        int saverId = name.hashCode();
        while (saverMap.containsKey(saverId)) {
            saverId++;
        }

        saverMap.put(saverId, saver);
        saverNameById.put(saverId, name);
        saver.setSaverId(saverId);

        return saverId;
    }

    public static String getSaverName(int saverId) {
        return saverNameById.get(saverId);
    }



    static Object unwrapIfNeeded(Object object) {
        if (object instanceof Wrapper) {
            return ((Wrapper) object).unwrap();
        }
        return object;
    }

    public static ObjectSaver getSaverFor(Object object) {
        ObjectSaver saver = saverByObjectHash.get(object.hashCode());
        if (saver != null) {
            return saver;
        }
        if (object instanceof ScriptableObject) {
            Object val = ((ScriptableObject) object).get(PROPERTY_SAVER_ID);
            if (val instanceof Number) {
                int id = ((Number) val).intValue();
                return saverMap.get(id);
            }
        }
        return null;
    }

    public static ScriptableObject saveObject(Object object) {
        object = unwrapIfNeeded(object);

        ObjectSaver saver = getSaverFor(object);
        if (saver != null) {
            ScriptableObject result = null;
            try {
                result = saver.save(object);
            } catch(Throwable err) {
                WorldDataSaver.logErrorStatic("error in saving object of saver type " + getSaverName(saver.getSaverId()), err);
                return null;
            }
            if (result != null) {
                result.put(PROPERTY_SAVER_ID, result, saver.getSaverId());
            }
            return result;
        }
        return object instanceof ScriptableObject ? (ScriptableObject) object : null;
    }

    // unwrap object if required, run saver, if exists, or just return object itself
    public static Object saveOrSkipObject(Object object) {
        object = unwrapIfNeeded(object);
        if (object == null || object == Undefined.instance) {
            return null;
        }

        ObjectSaver saver = getSaverFor(object);
        if (saver != null) {
            ScriptableObject result = null;
            try {
                result = saver.save(object);
            } catch(Throwable err) {
                WorldDataSaver.logErrorStatic("error in saving object of saver type " + getSaverName(saver.getSaverId()), err);
                return null;
            }
            if (result != null) {
                result.put(PROPERTY_SAVER_ID, result, saver.getSaverId());
            }
            return result;
        }
        return object;
    }

    public static ScriptableObject saveObjectAndCheckSaveIgnoring(Object object) {
        if (object instanceof ScriptableObject) {
            Object val = ((ScriptableObject) object).get(PROPERTY_IGNORE_SAVE);
            if (val instanceof Boolean && (Boolean) val) {
                return null;
            }
        }
        return saveObject(object);
    }

    public static Scriptable readObject(ScriptableObject object) {
        ObjectSaver saver = getSaverFor(object);
        if (saver != null) {
            Object result = null;
            try {
                result = saver.read(object);
            } catch(Throwable err) {
                WorldDataSaver.logErrorStatic("error in reading object of saver type " + getSaverName(saver.getSaverId()), err);
                return null;
            }
            if (!(result instanceof Scriptable)) {
                result = Context.javaToJS(result, scope);
                if (!(result instanceof Scriptable)) {
                    return null;
                }
            }
            return (Scriptable) result;
        }
        return object;
    }

    public static void registerObject(Object object, int saverId) {
        if (!saverMap.containsKey(saverId)) {
            throw new IllegalArgumentException("no saver found for id " + saverId + " use only registerObjectSaver return values");
        }
        object = unwrapIfNeeded(object);
        saverByObjectHash.put(object.hashCode(), saverMap.get(saverId));
    }

    public static void setObjectIgnored(ScriptableObject object, boolean ignore) {
        object.put(PROPERTY_IGNORE_SAVE, object, ignore);
    }
}
