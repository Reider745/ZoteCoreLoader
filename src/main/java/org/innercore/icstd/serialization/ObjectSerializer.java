package org.innercore.icstd.serialization;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import org.json.JSONObject;
import org.mozilla.javascript.*;

import java.util.*;

public class ObjectSerializer {
    private static final HashMap<String, SerializerRecord> serializerMap = new HashMap<>();
    private static final HashMap<Integer, SerializerRecord> legacyHashIdLookup = new HashMap<>();

    public static final String SKIP_SERIALIZATION_TYPE_ID = registerType("__serialization_skip", Object.class, new IObjectSerializer<Object>() {
        @Override public Object read(IScopedReader reader, SerializationContext ctx, String typeId) { return null; }
        @Override public void write(IScopedWriter writer, Object object, SerializationContext ctx, String typeId) { writer.endScope(); }
    }, false);

    public static class SerializerRecord {
        private IObjectSerializer<?> serializer;
        private Class<?> objectType;
        private String typeId;
        private int legacyHashId;

        public Class<?> getObjectType() {
            return objectType;
        }

        public IObjectSerializer<?> getSerializer() {
            return serializer;
        }

        public String getTypeId() {
            return typeId;
        }

        public int getLegacyHashId() {
            return legacyHashId;
        }
    }

    public static SerializerRecord getSerializerRecordByTypeId(String typeId) {
        return serializerMap.get(typeId);
    }

    public static SerializerRecord getSerializerRecordByLegacyHashId(int legacyHashId) {
        return legacyHashIdLookup.get(legacyHashId);
    }

    public static boolean isTypeRegistered(String saverTypeId) {
        return getSerializerRecordByTypeId(saverTypeId) != null;
    }

    public static IObjectSerializer<?> getSerializerByTypeId(String typeId) {
        SerializerRecord rec = getSerializerRecordByTypeId(typeId);
        return rec != null ? rec.serializer : null;
    }

    public static<T> IObjectSerializer<T> getSerializerByTypeId(String typeId, Class<T> objectType) {
        SerializerRecord rec = getSerializerRecordByTypeId(typeId);
        return rec != null && objectType.isAssignableFrom(rec.objectType) ? (IObjectSerializer<T>) rec.serializer : null;
    }

    public static String getTypeIdFromLegacyHashId(int legacyHashId, String fallback) {
        SerializerRecord rec = getSerializerRecordByLegacyHashId(legacyHashId);
        return rec != null ? rec.typeId : fallback;
    }

    public static String getTypeIdFromLegacyHashIdNotNull(int legacyHashId) {
        String typeId = getTypeIdFromLegacyHashId(legacyHashId, null);
        return typeId != null ? typeId : "__unknown_hash_id:" + legacyHashId;
    }

    public static int getLegacyHashIdFromTypeId(String typeId, int fallback) {
        SerializerRecord rec = getSerializerRecordByTypeId(typeId);
        return rec != null ? rec.legacyHashId : fallback;
    }


    public static<T> String registerType(String typeId, Class<T> cls, IObjectSerializer<T> saver, boolean canFail) {
        int legacyHashId = typeId.hashCode();
        if (getSerializerRecordByTypeId(typeId) != null) {
            if (!canFail) {
                throw new IllegalArgumentException("Saver " + typeId + " was already occupied and cannot be registered twice!");
            }
            return null;
        }

        while (legacyHashIdLookup.containsKey(legacyHashId)) {
            SerializerRecord rec = getSerializerRecordByLegacyHashId(legacyHashId);
            ICLog.i("SAVER", "object saver legacy hash id collision (this will only affect loading from legacy saves) '" + typeId + "' and '" + rec.typeId + "' id=" + legacyHashId);
            if (legacyHashId == 0) legacyHashId = 1;
            legacyHashId ^= (legacyHashId << 13);
            legacyHashId ^= (legacyHashId >>> 17);
            legacyHashId ^= (legacyHashId << 5);
        }

        SerializerRecord rec = new SerializerRecord();
        rec.serializer = saver;
        rec.objectType = cls;
        rec.legacyHashId = legacyHashId;
        rec.typeId = typeId;
        serializerMap.put(typeId, rec);
        legacyHashIdLookup.put(legacyHashId, rec);
        return typeId; // allow assigning it to variable
    }

    public static<T> String registerType(String typeId, Class<T> cls, IObjectSerializer<T> saver) {
        return registerType(typeId, cls, saver, false);
    }



    // NOTE: Null is not a primitive type, instead it is represented by creating an empty scope (writer.beginScope(...).endScope())
    public static boolean isPrimitiveType(Object o) {
        return o instanceof Number || o instanceof Boolean || o instanceof CharSequence;
    }

    public static class PackedObjectReader {
        private final IScopedReader originalReader;
        public final IScopedReader reader;
        public final String typeId;

        private PackedObjectReader(IScopedReader originalReader, IScopedReader reader, String typeId) {
            this.originalReader = originalReader;
            this.reader = reader;
            this.typeId = typeId;
        }

        // must be called after reader.endScope()
        public void endScope() {
            if (reader != originalReader) {
                originalReader.endScope();
            }
        }
    }

    // NOTE: returns null and leaves reader untouched, if it is not a packed object
    public static PackedObjectReader unpackObjectAndTypeId(IScopedReader reader, SerializationContext ctx) {
        IArrayReader array = reader.asArray();
        String typeId = null;
        IScopedReader target = reader;
        if (array != null && array.size() == 2 && array.getValueType(0).isString() && array.getValueType(1).isScope()) {
            String str = array.getString(0, "");
            if (str.startsWith("_t:")) {
                typeId = str.substring(3);
                target = array.beginScope(1);
            }
        }
        if (ctx.hasLegacyFormat) {
            IObjectReader object = reader.asObject();
            if (object != null && object.getValueType(ObjectSaverRegistry.PROPERTY_SAVER_ID).isNumber()) {
                int legacyTypeId = object.getInt(ObjectSaverRegistry.PROPERTY_SAVER_ID, 0);
                typeId = getTypeIdFromLegacyHashIdNotNull(legacyTypeId);
            }
        }
        if (typeId == null) {
            return null;
        }
        return new PackedObjectReader(reader, target, typeId);
    }

    // NOTE: in non-legacy format returns a new child writer for the payload, in legacy format returns the same writer.
    // Each *distinct* writer must call end scope, starting from one returned, and then passed
    public static IScopedWriter packObjectAndTypeId(IScopedWriter writer, String typeId, SerializationContext ctx) {
        if (typeId == null) {
            throw new IllegalArgumentException("typeId is null");
        }
        if (ctx.forceLegacyFormat) {
            IObjectWriter object = writer.asObject();
            SerializerRecord rec = getSerializerRecordByTypeId(typeId);
            object.putAny(ObjectSaverRegistry.PROPERTY_SAVER_ID, rec != null ? rec.legacyHashId : -2); // -2 is a stub value
            return writer;
        } else {
            IArrayWriter array = writer.asArray();
            array.addAny("_t:" + typeId);
            return array.beginScope();
        }
    }

    public static String getSerializerTypeIdFromObject(Object src) {
        if (src instanceof ISerializableObject) {
            String typeId = ((ISerializableObject) src).getSerializationTypeId();
            if (typeId != null)
                return typeId;
        }
        if (src instanceof ObjectSaverRegistry.IHashSaver) {
            int hashId = ((ObjectSaverRegistry.IHashSaver) src).getObjectSaverId();
            return getTypeIdFromLegacyHashIdNotNull(hashId);
        } else if (src instanceof Scriptable) {
            Scriptable scriptable = (Scriptable) src;
            Object saverId = scriptable.get(ObjectSaverRegistry.PROPERTY_SAVER_ID, scriptable);
            if (saverId != Scriptable.NOT_FOUND) {
                if (saverId instanceof CharSequence) {
                    return saverId.toString();
                } else if (saverId instanceof Number) {
                    return getTypeIdFromLegacyHashIdNotNull(((Number) saverId).intValue());
                }
            }
        } else if (src instanceof JSONObject) {
            Object saverId = ((JSONObject) src).opt(ObjectSaverRegistry.PROPERTY_SAVER_ID);
            if (saverId instanceof String) {
                return (String) saverId;
            } else if (saverId instanceof Number) {
                return getTypeIdFromLegacyHashIdNotNull(((Number) saverId).intValue());
            }
        } else if (src instanceof Wrapper) {
            return getSerializerTypeIdFromObject(((Wrapper) src).unwrap());
        }
        return null;
    }


    public static boolean serializeObjectUnpacked(IScopedWriter io, Object src, String typeId, SerializationContext ctx, boolean optional) {
        if (!io.isInScope())
            throw new IllegalStateException("writer scope is already closed");
        if (src == null) {
            io.endScope();
            throw new IllegalArgumentException("serializeObjectUnpacked - object is null");
        }
        SerializerRecord rec = getSerializerRecordByTypeId(typeId);
        if (rec == null) {
            String msg = "serializeObject: unknown saver type will be skipped " + typeId;
            io.endScope();
            if (!optional) throw new IllegalArgumentException(msg);
            else ctx.addError(msg);
            return false;
        }
        if (!rec.objectType.isAssignableFrom(src.getClass())) {
            String msg = "serializeObject: write() of serializer " + rec.serializer.getClass() + " expects " + rec.objectType + " but passed " + src.getClass();
            io.endScope();
            if (!optional) throw new IllegalArgumentException(msg);
            else ctx.addError(msg);
            return false;
        }
        boolean error = false;
        try {
            ((IObjectSerializer<Object>) rec.serializer).write(io, src, ctx, typeId);
        } catch (Throwable err) {
            error = true;
            if (!optional)
                throw err;
            ctx.addError(err, src, "serializer.write() failed with error");
        } finally {
            if (io.isInScope()) {
                if (!error) ctx.addError(null, rec.serializer, "serializer didn't close its scope after exiting " + rec.serializer.getClass());
                io.forceEndScope(ctx);
            }
        }
        return !error;
    }

    public static boolean serializeObject(IScopedWriter io, Object o, SerializationContext ctx, boolean optional) {
        if (!io.isInScope())
            throw new IllegalStateException("writer scope is already closed");
        if (o == null) {
            io.endScope();
            if (optional) return true;
            else throw new IllegalArgumentException("serializeObject(): non-nullable object is null (use serializeObjectOpt to write null)");
        }
        String typeId = getSerializerTypeIdFromObject(o);
        if (typeId == null) {
            io.endScope();
            String msg = "object don't have associated saver type";
            if (optional) ctx.addError(null, o, msg);
            else throw new IllegalArgumentException(msg);
            return false;
        }
        IScopedWriter writer = packObjectAndTypeId(io, typeId, ctx);
        boolean success;
        try {
            success = serializeObjectUnpacked(writer, o, typeId, ctx, optional);
        } finally {
            if (writer != io)
                io.endScope();
        }
        return success;
    }

    // nullable, never throws (serializer errors are swallowed)
    public static boolean serializeObjectOpt(IScopedWriter writer, Object o) {
        return serializeObject(writer, o, new SerializationContext(), true);
    }

    // non-null, throws on null or any serializer error
    public static void serializeObjectStrict(IScopedWriter writer, Object o) {
        serializeObject(writer, o, new SerializationContext(), false);
    }

    public static<T> T deserializeObjectUnpacked(IScopedReader io, String typeId, Class<T> cls, SerializationContext ctx, boolean optional) {
        if (!io.isInScope())
            throw new IllegalStateException("deserializeObjectUnpacked(): reader scope is already closed");
        SerializerRecord rec = getSerializerRecordByTypeId(typeId);
        if (rec == null) {
            io.endScope();
            String msg = "serializer not found for type " + typeId;
            if (optional) ctx.addError(msg);
            else throw new IllegalArgumentException("deserializeObjectUnpacked(): " + msg);
            return null;
        }
        if (!cls.isAssignableFrom(rec.objectType)) {
            io.endScope();
            String msg = "read() of serializer " + rec.serializer.getClass() + " returns " + rec.objectType + " but expected " + cls;
            if (optional) ctx.addError(msg);
            else throw new IllegalArgumentException("deserializeObjectUnpacked(): " + msg);
            return null;
        }
        Object result = null;
        boolean error = false;
        try {
            result = rec.serializer.read(io, ctx, typeId);
        } catch (Throwable err) {
            if (!optional)
                throw err;
            ctx.addError(err, null, "serializer.read() failed with error");
            error = true;
        }
        finally {
            if (io.isInScope()) {
                if (!error) ctx.addError(null, rec.serializer, "deserializer didn't close its scope after exiting " + rec.serializer.getClass());
                io.forceEndScope(ctx);
            }
        }
        if (result == null) {
            String msg = "serializer.read() returned null";
            if (!optional) throw new NullPointerException("deserializeObjectUnpacked(): " + msg);
        }
        if (result == null || cls.isInstance(result)) {
            return (T) result;
        } else {
            String msg = "object deserialized into unexpected type " + result.getClass() + " (expected " + cls + ")";
            if (optional) ctx.addError(msg);
            else throw new IllegalStateException("deserializeObjectUnpacked(): " + msg);
            return null;
        }
    }

    public static<T> T deserializeObject(IScopedReader io, Class<T> cls, SerializationContext ctx, boolean optional) {
        if (!io.isInScope())
            throw new IllegalStateException("deserializeObject(): reader scope is already closed");
        if (io.isEmpty()) {
            io.endScope();
            if (optional) return null;
            else throw new IllegalStateException("deserializeObject(): non-nullable object scope is empty");
        }
        PackedObjectReader packed = unpackObjectAndTypeId(io, ctx);
        if (packed == null) {
            io.endScope();
            String msg = "not a packed serialized object";
            if (optional) ctx.addError(null, io, msg);
            else throw new IllegalStateException("deserializeObject(): " + msg);
            return null;
        }
        T result;
        try {
            result = deserializeObjectUnpacked(packed.reader, packed.typeId, cls, ctx, optional);
        } finally {
            packed.endScope();
        }
        return result;
    }

    // nullable, never throws
    public static<T> T deserializeObjectOpt(IScopedReader io, Class<T> cls) {
        return deserializeObject(io, cls, new SerializationContext(), true);
    }

    // not-null, throws on any error or when null is read
    public static<T> T deserializeObjectStrict(IScopedReader io, Class<T> cls) {
        return deserializeObject(io, cls, new SerializationContext(), false);
    }
}
