package org.innercore.icstd.serialization;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import org.json.JSONObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import java.util.ArrayList;
import java.util.List;

public abstract class ScriptableObjectSerializer implements IObjectSerializer<Object> {
    // silently convert number scriptable object keys to strings in serializer
    public static final boolean ALLOW_NUMBER_KEYS_TO_STRING_CONVERSION = true;

    // js arrays are sparse and can have absurd length, writing them as dense json must not hang the save
    public static final long MAX_SERIALIZED_ARRAY_LENGTH = 0x100000;

    private static void writeImpl(IScopedWriter io, Object src, SerializationContext ctx, boolean ignoreThisSaver) {
        if (src == null || src == Undefined.instance) {
            io.endScope(); // null scope
            return;
        }
        boolean addedToVisited = false;
        if (src instanceof ScriptableObject) {
            addedToVisited = ctx.visitedObjects.add(src);
            if (!addedToVisited && !ignoreThisSaver) {
                ctx.addError(null, src, "cyclic reference found");
                io.endScope();
                return;
            }
        }
        if (!ignoreThisSaver) {
            String saverId = ObjectSerializer.getSerializerTypeIdFromObject(src);
            if (saverId != null && ObjectSerializer.isTypeRegistered(saverId)) {
                ObjectSerializer.serializeObject(io, src, ctx, /* can fail */ true);
                if (addedToVisited) ctx.visitedObjects.remove(src);
                return;
            }
        }
        if (src instanceof NativeArray) {
            NativeArray array = (NativeArray) src;
            IArrayWriter writer = io.asArray();
            long length = array.getLength();
            if (length > MAX_SERIALIZED_ARRAY_LENGTH) {
                ctx.addError(null, src, "array is too long to be serialized and will be truncated: length=" + length);
                length = MAX_SERIALIZED_ARRAY_LENGTH;
            }
            for (long i = 0; i < length; i++) {
                Object o = array.get(i);
                if (ObjectSerializer.isPrimitiveType(o)) {
                    writer.addAny(o);
                } else {
                    writeImpl(writer.beginScope(), o, ctx, false);
                }
            }
            io.endScope();
            if (addedToVisited) ctx.visitedObjects.remove(src);
            return;
        }
        if (src instanceof ScriptableObject) {
            ScriptableObject scriptable = (ScriptableObject) src;
            IObjectWriter writer = io.asObject();
            for (Object id : scriptable.getIds()) {
                // value must be fetched with the original id: number ids are index slots,
                // which a string lookup would miss
                Object o = scriptable.get(id);
                if (!(id instanceof String)) {
                    if (ALLOW_NUMBER_KEYS_TO_STRING_CONVERSION) {
                        id = id + "";
                    } else {
                        ctx.addError(null, id, "serialized scriptable object can't have number ids");
                        continue;
                    }
                }
                if (ObjectSerializer.isPrimitiveType(o)) {
                    writer.putAny((String) id, o);
                } else {
                    writeImpl(writer.beginScope((String) id), o, ctx, false);
                }
            }
            io.endScope();
            if (addedToVisited) ctx.visitedObjects.remove(src);
            return;
        }
        ctx.addError(null, src, "invalid object can't be serialized and will be skipped " + (src != null ? src.getClass() : null));
        io.endScope();
    }

    public static void writeScriptable(IScopedWriter writer, Object object, SerializationContext ctx) {
        writeImpl(writer, object, ctx, false);
    }

    @Override
    public void write(IScopedWriter writer, Object object, SerializationContext ctx, String typeId) {
        writeImpl(writer, save(object), ctx, true);
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
                Object value;
                if (reader.getValueType(key).isScope()) {
                    value = readImpl(reader.beginScope(key), ctx, false);
                } else {
                    value = reader.getAny(key, null);
                }
                // number keys (written as strings by ALLOW_NUMBER_KEYS_TO_STRING_CONVERSION) must go
                // back into index slots, a string put would make them invisible to js element access
                long index = ScriptRuntime.indexFromString(key);
                if (index >= 0 && index <= Integer.MAX_VALUE) {
                    object.put((int) index, object, value);
                } else {
                    object.put(key, object, value);
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
        if (result == null) {
            // nothing was saved (e.g. save() returned null), nothing to restore
            return null;
        }
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
