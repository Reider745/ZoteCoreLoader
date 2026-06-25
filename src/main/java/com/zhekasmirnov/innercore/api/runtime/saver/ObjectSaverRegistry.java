package com.zhekasmirnov.innercore.api.runtime.saver;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.innercore.icstd.inventory.ItemInstanceExtra;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ui.container.Container;
import org.innercore.icstd.serialization.IObjectSerializer;
import org.innercore.icstd.serialization.ObjectSerializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.*;

/**
 * Created by zheka on 19.08.2017.
 */

public class ObjectSaverRegistry {
    @Deprecated
    public static final String PROPERTY_IGNORE_SAVE = "_json_ignore";
    public static final String PROPERTY_SAVER_ID = "_json_saver_id";
    @Deprecated
    public static final String PROPERTY_SAVER_ENABLE = PROPERTY_SAVER_ID;//"_json_saver_enable";

    static {
        Container.initSaverId();
        ItemInstanceExtra.initSaverId();
    }

    @Deprecated
    public interface IHashSaver {
        @Deprecated
        int getObjectSaverId();
        @Deprecated
        void setObjectSaverId(int id);
    }

    public static String registerSaverStr(String name, ObjectSaver saver) {
        ObjectSerializer.registerType(name, Object.class, saver, false);
        saver.setSaverId(ObjectSerializer.getLegacyHashIdFromTypeId(name, -1));
        return name;
    }

    @Deprecated
    public static int registerSaver(String name, ObjectSaver saver) {
        registerSaverStr(name, saver);
        return saver.getSaverId();
    }

    @Deprecated
    public static String getSaverName(int saverId) {
        return ObjectSerializer.getTypeIdFromLegacyHashId(saverId, null);
    }

    @Deprecated
    static Object unwrapIfNeeded(Object object) {
        if (object instanceof Wrapper) {
            return ((Wrapper) object).unwrap();
        }
        return object;
    }

    @Deprecated
    public static ObjectSaver getSaverFor(Object object) {
        String typeId = ObjectSerializer.getSerializerTypeIdFromObject(object);
        if (typeId != null) {
            IObjectSerializer<?> serializer = ObjectSerializer.getSerializerByTypeId(typeId);
            if (serializer instanceof ObjectSaver) {
                return (ObjectSaver) serializer;
            }
        }
        return null;
    }

    @Deprecated
    public static void registerObject(Object object, String saverId) {
        if (!ObjectSerializer.isTypeRegistered(saverId)) {
            throw new IllegalArgumentException("no saver found for id " + saverId + " use only registerObjectSaver return values");
        }
        object = unwrapIfNeeded(object);
        if (object instanceof ScriptableObject){
            final ScriptableObject scriptable = (ScriptableObject) object;
            scriptable.put(PROPERTY_SAVER_ID, scriptable, saverId);
        }else if(object instanceof JSONObject){
            try {
                ((JSONObject) object).put(PROPERTY_SAVER_ID, saverId);
            } catch (JSONException e) {
                ICLog.e("SAVER", "error get PROPERTY_SAVER_ENABLE", e);
            }
        }else if(object instanceof IHashSaver){
            int legacySaverId = ObjectSerializer.getLegacyHashIdFromTypeId(saverId, -1);
            if (legacySaverId == -1) {
                legacySaverId = ObjectSerializer.getLegacyHashIdFromTypeId(ObjectSerializer.SKIP_SERIALIZATION_TYPE_ID, -1);
            }
            ((IHashSaver) object).setObjectSaverId(legacySaverId);
        }else {
            Logger.warning("Error registerObject "+ object.getClass().getName() + ", serverId: "+saverId);
        }
    }

    @Deprecated
    public static void setObjectIgnored(ScriptableObject object, boolean ignore) {
        // NOTE: This is not working for a long time already
        // object.put(PROPERTY_IGNORE_SAVE, object, ignore);
    }
}
