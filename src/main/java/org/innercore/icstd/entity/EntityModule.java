package org.innercore.icstd.entity;

import com.zhekasmirnov.innercore.api.gen.EntityBindGenBase;
import lombok.NonNull;
import org.innercore.icstd.annotations.ApiBinding;
import org.innercore.icstd.annotations.ApiHidden;
import org.innercore.icstd.event.Events;
import org.innercore.icstd.event.impl.entity.EntityRemovedEvent;
import org.innercore.icstd.event.impl.level.LevelLeftEvent;
import org.innercore.icstd.serialization.*;
import org.innercore.internal.serialization.JsonIO;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;

import java.util.HashMap;
import java.util.Map;

@ApiBinding("Entity")
public class EntityModule extends EntityBindGenBase {
    private static final String SCRIPTABLE_TYPE = ObjectSerializer.registerType("ic:scriptable", Scriptable.class, new IObjectSerializer<Scriptable>() {
        @Override
        public Scriptable read(IScopedReader reader, SerializationContext ctx, String typeId) {
            return (Scriptable) ScriptableObjectSerializer.readScriptable(reader, ctx);
        }

        @Override
        public void write(IScopedWriter writer, Scriptable object, SerializationContext ctx, String typeId) {
            ScriptableObjectSerializer.writeScriptable(writer, object, ctx);
        }
    });

    @ApiHidden
    public static class EntityScopeData {
        private Object savedData;
        private String typeId;
        private Object value;

        @NonNull
        public Object getSavedData() {
            Object savedData = this.savedData;
            if(savedData == null) {
                JsonIO.ScopedWriter writer = new JsonIO.ScopedWriter();
                ObjectSerializer.serializeObjectUnpacked(writer, value, this.typeId, new SerializationContext(), false);

                this.savedData = writer.getResult();
                savedData = this.savedData;
            }

            return savedData;
        }

        public void setValue(Object value) {
            if(value == null) return;

            if(value instanceof JSONObject || value instanceof JSONArray) {
                this.savedData = value;
            } else {
                this.savedData = null;
            }

            if(value instanceof Scriptable) {
                this.typeId = SCRIPTABLE_TYPE;
            } else {
                this.typeId = ObjectSerializer.getSerializerTypeIdFromObject(value);
            }

            this.value = value;
        }

        public String getTypeId() {
            return this.typeId;
        }

        @NonNull
        public Object getValue(Object def) {
            if(this.value == null) return def;
            return this.value;
        }
    }

    @ApiHidden
    public static class EntityUserData {
        private JSONObject savedData;
        private JSONObject savedTypedId;
        private final Map<String, EntityScopeData> userObjects = new HashMap<>();

        @NonNull
        public JSONObject getSavedData() {
            JSONObject savedData = this.savedData;
            if(savedData == null) {
                savedData = new JSONObject();

                final JSONObject finalSavedData = savedData;
                userObjects.forEach((scope, scopeData) -> {
                    try {
                        finalSavedData.put(scope, scopeData.getSavedData());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });

                this.savedData = savedData;
                savedData = this.savedData;
            }

            return savedData;
        }

        @NonNull
        public Object getSavedData(String scope) {
            EntityScopeData scopeData = userObjects.get(scope);
            if(scopeData != null) {
                return scopeData.getSavedData();
            }
            return new JSONObject();
        }

        @NonNull
        public JSONObject getSavedTypedId() {
            JSONObject savedData = this.savedTypedId;
            if(savedData == null) {
                savedData = new JSONObject();

                final JSONObject finalSavedData = savedData;
                userObjects.forEach((scope, scopeData) -> {
                    try {
                        String typeId = scopeData.getTypeId();
                        if(typeId != null) {
                            finalSavedData.put(scope, typeId);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });

                this.savedTypedId = savedData;
                savedData = this.savedTypedId;
            }

            return savedData;
        }

        public void setValue(String scope, Object value) {
            if(value instanceof JSONObject) {
                this.savedData = (JSONObject) value;
            } else {
                this.savedData = null;
            }
            this.savedTypedId = null;
            userObjects.computeIfAbsent(scope, key -> new EntityScopeData()).setValue(value);
        }

        @NonNull
        public Object getValue(String scope, Object def) {
            this.savedData = null;

            EntityScopeData data = userObjects.get(scope);
            if(data == null) return def;
            return data.getValue(def);
        }

        public boolean hasScope(String scope) {
            return userObjects.containsKey(scope);
        }

        public void remove(String scope) {
            userObjects.remove(scope);
        }

        public void clearAll(String scope) {
            this.remove(scope);
        }
    }

    private static final Map<Long, EntityUserData> entityUserData = new HashMap<>();

    static {
        Events.on(EntityRemovedEvent.class, event -> {
            entityUserData.remove(event.getEntityUid());
        });

        Events.on(LevelLeftEvent.class, event -> {
            if(event.isServer()) {
                entityUserData.clear();
            }
        });
    }

    public static void loadClass() {}

    @ApiHidden
    public static Map<Long, EntityUserData> getEntityUserData() {
        return entityUserData;
    }

    public static IScopedWriter writeUserData(long uid, String scope) {
        return new JsonIO.ScopedWriter() {
            @Override
            public void endScope() {
                super.endScope();

                entityUserData.computeIfAbsent(uid, key -> new EntityUserData()).setValue(scope, this.getResult());
            }
        };
    }

    public static IScopedReader readUserData(long uid, String scope) {
        EntityUserData data = entityUserData.get(uid);
        if(data == null) return null;
        return new JsonIO.ScopedReader(data.getSavedData(scope));
    }


    @ApiBinding
    public static void putCustomComponent(long uid, String scope, Object data) {
        entityUserData.computeIfAbsent(uid, key -> new EntityUserData()).setValue(scope, data);
    }

    @Nullable
    public static <T>T getCustomComponent(long uid, String scope, T def) {
        EntityUserData data = entityUserData.get(uid);
        if(data == null) return def;
        return (T) data.getValue(scope, def);
    }

    @Nullable
    public static <T>T getCustomComponent(long uid, String scope) {
        return getCustomComponent(uid, scope, null);
    }

    @ApiBinding("writeUserData")
    public static void scriptWriteUserData(long uid, String scope, Object value, String typeId) {
        ObjectSerializer.serializeObjectUnpacked(new JsonIO.ScopedWriter() {
            @Override
            public void endScope() {
                super.endScope();

                entityUserData.computeIfAbsent(uid, key -> new EntityUserData()).setValue(scope, this.getResult());
            }
        }, value, typeId, new SerializationContext(), false);
    }

    @ApiBinding("readUserData")
    public static Object scriptReadUserData(long uid, String scope, String typeId) {
        EntityUserData data = entityUserData.get(uid);
        if(data == null) return null;
        return ObjectSerializer.deserializeObjectUnpacked(new JsonIO.ScopedReader(data.getSavedData(scope)), typeId, Object.class, new SerializationContext(), false);
    }

    @Nullable
    @ApiBinding("getCustomComponent")
    public static Object scriptGetCustomComponent(long uid, String scope, Object def) {
        EntityUserData data = entityUserData.get(uid);
        if(data == null) return def;
        return data.getValue(scope, def);
    }

    @Nullable
    @ApiBinding("getCustomComponent")
    public static Object scriptGetCustomComponent(long uid, String scope) {
        return getCustomComponent(uid, scope, null);
    }
}
