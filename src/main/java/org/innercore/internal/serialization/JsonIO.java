package org.innercore.internal.serialization;

import org.innercore.icstd.serialization.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class JsonIO {
    // WRITER

    private static boolean isScopeValue(Object o) {
        return o instanceof JSONObject || o instanceof JSONArray || o == null || o == JSONObject.NULL;
    }

    private static boolean isNonEmptyScopeValue(Object o) {
        return o instanceof JSONObject || o instanceof JSONArray;
    }

    private static float sanitizeFloat(float value) {
        if (!Float.isFinite(value)) {
            value = 0;
        }
        return value;
    }

    private static double sanitizeDouble(double value) {
        if (!Double.isFinite(value)) {
            value = 0;
        }
        return value;
    }

    private static Object validateJsonValue(Object value) {
        if (value instanceof CharSequence) {
            value = value.toString();
        } else if (value instanceof Number) {
            if (!(value instanceof Long) && !Double.isFinite(((Number) value).doubleValue()))
                value = 0.f;
        } else if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("passed invalid non-trivial value to json writer: " + value);
        }
        return value;
    }

    public static class ScopedWriter extends BaseIO.ScopedWriter {
        public ScopedWriter() {
            super();
        }

        public ScopedWriter(BaseIO.BaseWriter parent) {
            super(parent);
        }

        public Object getResult() {
            return resultObj;
        }

        @Override
        protected BaseIO.BaseWriter newObjectWriter(BaseIO.ScopedWriter parent) {
            return new ObjectWriter(parent);
        }

        @Override
        protected BaseIO.BaseWriter newArrayWriter(BaseIO.ScopedWriter parent) {
            return new ArrayWriter(parent);
        }

        @Override
        protected BaseIO.ScopedWriter newScopedWriter(BaseIO.BaseWriter parent) {
            return new ScopedWriter(parent);
        }
    }

    public static class ObjectWriter extends BaseIO.BaseWriter implements IObjectWriter {
        private String childScopeKey;
        private final HashMap<String, Object> data = new HashMap<>();

        ObjectWriter(BaseIO.ScopedWriter scope) { super(scope); }

        private IObjectWriter putAnyImpl(String key, Object value) {
            if (childScope != null) throw new IllegalStateException("in child scope");
            else if (scope == null) throw new IllegalStateException("scope is closed");
            data.put(key, value);
            return this;
        }

        @Override public IObjectWriter putInt(String key, int value) { return putAnyImpl(key, value); }
        @Override public IObjectWriter putLong(String key, long value) { return putAnyImpl(key, value); }
        @Override public IObjectWriter putShort(String key, short value) { return putAnyImpl(key, value); }
        @Override public IObjectWriter putByte(String key, byte value) { return putAnyImpl(key, value); }
        @Override public IObjectWriter putFloat(String key, float value) { return putAnyImpl(key, sanitizeFloat(value)); }
        @Override public IObjectWriter putDouble(String key, double value) { return putAnyImpl(key, sanitizeDouble(value)); }
        @Override public IObjectWriter putBoolean(String key, boolean value) { return putAnyImpl(key, value); }
        @Override public IObjectWriter putString(String key, String value) { return putAnyImpl(key, value); }

        @Override public boolean isPutAnySupported() { return true; }
        @Override public void putAny(String key, Object value) { putAnyImpl(key, validateJsonValue(value)); }

        @Override
        public IScopedWriter beginScope(String key) {
            Object o = data.get(key);
            if (isScopeValue(o) && data.containsKey(key)) throw new IllegalStateException("child scope '" + key + "' was already closed");
            IScopedWriter writer = beginScopeImpl();
            childScopeKey = key;
            return writer;
        }

        @Override
        protected Object endThisScope() {
            return new JSONObject(data);
        }

        @Override
        protected void writeEndedChildScope(Object childData) {
            data.put(childScopeKey, childData);
        }
    }

    public static class ArrayWriter extends BaseIO.BaseWriter implements IArrayWriter {
        private int childScopeIdx = 0;
        private final ArrayList<Object> data = new ArrayList<>();

        ArrayWriter(BaseIO.ScopedWriter scope) { super(scope); }

        @Override
        public int size() {
            return data.size();
        }

        private IArrayWriter addAnyImpl(Object value) {
            validateStateForWrite();
            data.add(validateJsonValue(value));
            return this;
        }

        private IArrayWriter addAnyImpl(int at, Object value) {
            validateStateForWrite();
            data.add(at, validateJsonValue(value));
            return this;
        }

        private IArrayWriter setAnyImpl(int idx, Object value) {
            validateStateForWrite();
            Object o = data.get(idx);
            if (o instanceof JSONObject || o instanceof JSONArray || o == null) {
                throw new IllegalStateException("array writer already has child scope at idx " + idx + " it can't be overridden with a value");
            }
            data.set(idx, validateJsonValue(value));
            return this;
        }

        @Override public IArrayWriter addInt(int value) { return addAnyImpl(value); }
        @Override public IArrayWriter addInt(int idx, int value) { return addAnyImpl(idx, value); }
        @Override public IArrayWriter setInt(int idx, int value) { return setAnyImpl(idx, value); }
        @Override public IArrayWriter addLong(long value) { return addAnyImpl(value); }
        @Override public IArrayWriter addLong(int idx, long value) { return addAnyImpl(idx, value); }
        @Override public IArrayWriter setLong(int idx, long value) { return setAnyImpl(idx, value); }
        @Override public IArrayWriter addShort(short value) { return addAnyImpl(value); }
        @Override public IArrayWriter addShort(int idx, short value) { return addAnyImpl(idx, value); }
        @Override public IArrayWriter setShort(int idx, short value) { return setAnyImpl(idx, value); }
        @Override public IArrayWriter addByte(byte value) { return addAnyImpl(value); }
        @Override public IArrayWriter addByte(int idx, byte value) { return addAnyImpl(idx, value); }
        @Override public IArrayWriter setByte(int idx, byte value) { return setAnyImpl(idx, value); }
        @Override public IArrayWriter addFloat(float value) { return addAnyImpl(value); }
        @Override public IArrayWriter addFloat(int idx, float value) { return addAnyImpl(idx, sanitizeFloat(value)); }
        @Override public IArrayWriter setFloat(int idx, float value) { return setAnyImpl(idx, sanitizeFloat(value)); }
        @Override public IArrayWriter addDouble(double value) { return addAnyImpl(value); }
        @Override public IArrayWriter addDouble(int idx, double value) { return addAnyImpl(idx, sanitizeDouble(value)); }
        @Override public IArrayWriter setDouble(int idx, double value) { return setAnyImpl(idx, sanitizeDouble(value)); }
        @Override public IArrayWriter addBoolean(boolean value) { return addAnyImpl(value); }
        @Override public IArrayWriter addBoolean(int idx, boolean value) { return addAnyImpl(idx, value); }
        @Override public IArrayWriter setBoolean(int idx, boolean value) { return setAnyImpl(idx, value); }
        @Override public IArrayWriter addString(String value) { return addAnyImpl(value); }
        @Override public IArrayWriter addString(int idx, String value) { return addAnyImpl(idx, value); }
        @Override public IArrayWriter setString(int idx, String value) { return setAnyImpl(idx, value); }

        @Override public boolean isPutAnySupported() { return true; }
        @Override public void addAny(Object value) { addAnyImpl(validateJsonValue(value)); }
        @Override public void addAny(int idx, Object value) { addAnyImpl(idx, validateJsonValue(value)); }
        @Override public void setAny(int idx, Object value) { setAnyImpl(idx, validateJsonValue(value)); }

        @Override
        public IScopedWriter beginScope() {
            return beginScope(size());
        }

        @Override
        public IScopedWriter beginScope(int idx) {
            IScopedWriter writer = beginScopeImpl();
            childScopeIdx = idx;
            return writer;
        }

        @Override
        protected void writeEndedChildScope(Object childData) {
            data.add(childScopeIdx, childData);
        }

        @Override
        protected Object endThisScope() {
            return new JSONArray(data);
        }
    }

    // READER

    public static class ScopedReader extends BaseIO.ScopedReader {
        public ScopedReader(BaseIO.BaseReader parent, Object data) {
            super(parent, data == JSONObject.NULL ? null : data);
            if (!isScopeValue(data)) {
                throw new IllegalArgumentException("data must be a JSONArray or JSONObject or null or JSONOOject.NULL got " + data.getClass());
            }
        }

        public ScopedReader(Object data) {
            super(data == JSONObject.NULL ? null : data);
            if (!isScopeValue(data)) {
                throw new IllegalArgumentException("data must be a JSONArray or JSONObject or null or JSONOOject.NULL got " + data.getClass());
            }
        }

        @Override
        protected BaseIO.BaseReader newObjectReader(BaseIO.ScopedReader parent, Object data) {
            return new ObjectReader(parent, data);
        }

        @Override
        protected BaseIO.BaseReader newArrayReader(BaseIO.ScopedReader parent, Object data) {
            return new ArrayReader(parent, data);
        }

        @Override
        protected BaseIO.ScopedReader newScopedReader(BaseIO.BaseReader parent, Object data) {
            return new ScopedReader(parent, data);
        }

        @Override public boolean isObject() {
            validateInScope();
            return data instanceof JSONObject;
        }

        @Override
        public boolean isArray() {
            validateInScope();
            return data instanceof JSONArray;
        }

        @Override
        public boolean isEmpty() {
            validateInScope();
            return data == null;
        }
    }

    public static class ObjectReader extends BaseIO.BaseReader implements IObjectReader {
        private final JSONObject data;
        ObjectReader(BaseIO.ScopedReader parent, Object data) { super(parent); this.data = (JSONObject) data; }

        @Override public boolean hasKey(String key) { return data.has(key); }

        @Override public ESerializedType getValueType(String key) {
            validateStateForRead();
            Object value = data.opt(key);
            if (value == null && !data.has(key))
                return ESerializedType.INVALID;
            return BaseIO.getSerializedTypeFromObject(value);
        }

        @Override public int getInt(String key, int fallback) {
            validateStateForRead();
            Object o = data.opt(key);
            return o instanceof Number ? ((Number) o).intValue() : fallback;
        }
        @Override public long getLong(String key, long fallback) {
            validateStateForRead();
            Object o = data.opt(key);
            return o instanceof Number ? ((Number) o).longValue() : fallback;
        }
        @Override public short getShort(String key, short fallback) {
            validateStateForRead();
            Object o = data.opt(key);
            return o instanceof Number ? ((Number) o).shortValue() : fallback;
        }
        @Override public byte getByte(String key, byte fallback) {
            validateStateForRead();
            Object o = data.opt(key);
            return o instanceof Number ? ((Number) o).byteValue() : fallback;
        }
        @Override public float getFloat(String key, float fallback) {
            validateStateForRead();
            Object o = data.opt(key);
            return o instanceof Number ? ((Number) o).floatValue() : fallback;
        }
        @Override public double getDouble(String key, double fallback) {
            validateStateForRead();
            Object o = data.opt(key);
            return o instanceof Number ? ((Number) o).doubleValue() : fallback;
        }
        @Override public boolean getBoolean(String key, boolean fallback) {
            validateStateForRead();
            Object o = data.opt(key);
            return o instanceof Boolean ? (Boolean) o : fallback;
        }
        @Override public String getString(String key, String fallback) {
            validateStateForRead();
            Object o = data.opt(key);
            return o instanceof String ? (String) o : fallback;
        }

        @Override public boolean isGetAnySupported() { return true; }
        @Override public Object getAny(String key, Object fallback) {
            validateStateForRead();
            Object o = data.opt(key);
            return isScopeValue(o) ? fallback : o;
        }

        @Override public Iterable<String> iterKeys() { return data::keys; }

        @Override
        public IScopedReader beginScope(String key) {
            validateStateForRead();
            Object o = data.opt(key);
            if (isScopeValue(o)) {
                return beginScopeImpl(o);
            } else {
                return beginScopeImpl(null);
            }
        }
    }

    public static class ArrayReader extends BaseIO.BaseReader implements IArrayReader {
        private final JSONArray data;
        ArrayReader(BaseIO.ScopedReader parent, Object data) { super(parent); this.data = (JSONArray) data; }

        @Override public int size() { return data.length(); }

        private void validateRead(int idx) {
            validateStateForRead();
            if (idx < 0 || idx >= data.length()) throw new IndexOutOfBoundsException("index out of range: " + idx + " len=" + data.length());
        }

        @Override
        public ESerializedType getValueType(int idx) {
            validateRead(idx);
            return BaseIO.getSerializedTypeFromObject(data.opt(idx));
        }

        @Override public int getInt(int idx, int fallback) {
            validateRead(idx);
            Object o = data.opt(idx);
            return o instanceof Number ? ((Number) o).intValue() : fallback;
        }
        @Override public long getLong(int idx, long fallback) {
            validateRead(idx);
            Object o = data.opt(idx);
            return o instanceof Number ? ((Number) o).longValue() : fallback;
        }
        @Override public short getShort(int idx, short fallback) {
            validateRead(idx);
            Object o = data.opt(idx);
            return o instanceof Number ? ((Number) o).shortValue() : fallback;
        }
        @Override public byte getByte(int idx, byte fallback) {
            validateRead(idx);
            Object o = data.opt(idx);
            return o instanceof Number ? ((Number) o).byteValue() : fallback;
        }
        @Override public float getFloat(int idx, float fallback) {
            validateRead(idx);
            Object o = data.opt(idx);
            return o instanceof Number ? ((Number) o).floatValue() : fallback;
        }
        @Override public double getDouble(int idx, double fallback) {
            validateRead(idx);
            Object o = data.opt(idx);
            return o instanceof Number ? ((Number) o).doubleValue() : fallback;
        }
        @Override public boolean getBoolean(int idx, boolean fallback) {
            validateRead(idx);
            Object o = data.opt(idx);
            return o instanceof Boolean ? (Boolean) o : fallback;
        }
        @Override public String getString(int idx, String fallback) {
            validateRead(idx);
            Object o = data.opt(idx);
            return o instanceof String ? (String) o : fallback;
        }

        @Override public boolean isGetAnySupported() { return true; }
        @Override public Object getAny(int idx, Object fallback) {
            validateRead(idx);
            Object o = data.opt(idx);
            return isScopeValue(o) ? fallback : o;
        }

        @Override
        public IScopedReader beginScope(int idx) {
            validateRead(idx);
            Object o = data.opt(idx);
            if (isScopeValue(o)) {
                return beginScopeImpl(o);
            } else {
                return beginScopeImpl(null);
            }
        }
    }
}
