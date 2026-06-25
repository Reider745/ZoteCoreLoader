package org.innercore.icstd.serialization;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import org.json.JSONObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import java.util.ArrayList;
import java.util.List;

public abstract class ScriptableObjectSerializer implements IObjectSerializer<Object> {
    private static void writeImpl(IScopedWriter io, Object src, SerializationContext ctx) {
        if (src == null || src == Undefined.instance) {
            io.endScope(); // null scope
            return;
        }
        if (src instanceof ScriptableObject && !ctx.visitedObjects.add(src)) {
            ctx.addError(null, src, "cyclic reference found");
            io.endScope();
            return;
        }
        Object saverId = ObjectSerializer.getSerializerTypeIdFromObject(src);
        if (saverId != null) {
            ObjectSerializer.serializeObject(io, src, ctx, /* can fail */ true);
            ctx.visitedObjects.remove(src);
            return;
        }
        if (src instanceof NativeArray) {
            NativeArray array = (NativeArray) src;
            IArrayWriter writer = io.asArray();
            for (int i = 0; i < array.getLength(); i++) {
                Object o = array.get(i);
                if (ObjectSerializer.isPrimitiveType(o)) {
                    writer.addAny(o);
                } else {
                    writeImpl(writer.beginScope(), o, ctx);
                }
            }
            io.endScope();
            ctx.visitedObjects.remove(src);
            return;
        }
        if (src instanceof ScriptableObject) {
            ScriptableObject scriptable = (ScriptableObject) src;
            IObjectWriter writer = io.asObject();
            for (Object id : scriptable.getIds()) {
                if (!(id instanceof String)) {
                    ctx.addError(null, id, "serialized scriptable object can't have integer ids");
                    continue;
                }
                Object o = scriptable.get(id);
                if (ObjectSerializer.isPrimitiveType(o)) {
                    writer.putAny((String) id, o);
                } else {
                    writeImpl(writer.beginScope((String) id), o, ctx);
                }
            }
            io.endScope();
            ctx.visitedObjects.remove(src);
            return;
        }
        ctx.addError(null, src, "invalid object can't be serialized and will be skipped " + (src != null ? src.getClass() : null));
        io.endScope();
    }

    public static void writeScriptable(IScopedWriter writer, Object object, SerializationContext ctx) {
        writeImpl(writer, object, ctx);
    }

    @Override
    public void write(IScopedWriter writer, Object object, SerializationContext ctx, String typeId) {
        writeImpl(writer, save(object), ctx);
    }

    private static Object readImpl(IScopedReader io, SerializationContext ctx, boolean ignoreThisSaver) {
        if (!ignoreThisSaver) {
            ObjectSerializer.PackedObjectReader packed = ObjectSerializer.unpackObjectAndTypeId(io, ctx);
            if (packed != null) {
                Object o = ObjectSerializer.deserializeObjectUnpacked(packed.reader, packed.typeId, Object.class, ctx, /* can fail */ true);
                packed.endScope();
                return o;
            }
        }
        if (io.isArray()) {
            IArrayReader reader = io.asArray();
            List<Object> array = new ArrayList<>(reader.size());
            for (int i = 0; i < reader.size(); i++) {
                if (reader.getValueType(i).isScope()) {
                    array.add(readImpl(reader.beginScope(i), ctx, false));
                } else {
                    array.add(reader.getAny(i, null));
                }
            }
            io.endScope();
            return ScriptableObjectHelper.createArray(array);
        }
        if (io.isObject()) {
            IObjectReader reader = io.asObject();
            ScriptableObject object = ScriptableObjectHelper.createEmpty();
            for (String key : reader.iterKeys()) {
                if (reader.getValueType(key).isScope()) {
                    object.put(key, object, readImpl(reader.beginScope(key), ctx, false));
                } else {
                    object.put(key, object, reader.getAny(key, null));
                }
            }
            io.endScope();
            return object;
        }
        if (io.isEmpty()) {
            io.endScope();
            return null;
        }
        ctx.addError(null, null, "scope is not an object nor an array");
        io.endScope();
        return null;
    }

    public static Object readScriptable(IScopedReader reader, SerializationContext ctx) {
        return readImpl(reader, ctx, false);
    }

    @Override
    public Object read(IScopedReader reader, SerializationContext ctx, String typeId) {
        Object result = readImpl(reader, ctx, true);
        if (result instanceof ScriptableObject) {
            Object o = read((ScriptableObject) result);
            if (o instanceof Scriptable && ObjectSerializer.getSerializerTypeIdFromObject(o) == null) {
                Scriptable s = (Scriptable) o;
                s.put(ObjectSaverRegistry.PROPERTY_SAVER_ID, s, typeId);
            }
            return o;
        } else {
            ctx.addError(null, result, "scriptable object saver can't read non-scriptable");
            return null;
        }
    }

    public abstract Object read(ScriptableObject input);
    public abstract ScriptableObject save(Object input);
}
