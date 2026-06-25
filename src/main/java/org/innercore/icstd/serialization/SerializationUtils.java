package org.innercore.icstd.serialization;

public class SerializationUtils {
    public static void readerToWriter(IScopedReader src, IScopedWriter dst) {
        if (src.isObject()) {
            IObjectReader srcObj = src.asObject();
            IObjectWriter dstObj = dst.asObject();
            boolean anySupported = srcObj.isGetAnySupported() && dstObj.isPutAnySupported();
            for (String key : srcObj.iterKeys()) {
                ESerializedType type = srcObj.getValueType(key);
                if (type.isScope()) {
                    readerToWriter(srcObj.beginScope(key), dstObj.beginScope(key));
                    continue;
                }
                if (anySupported) {
                    dstObj.putAny(key, srcObj.getAny(key, null));
                    continue;
                }
                switch (type) {
                    case INT: dstObj.putInt(key, srcObj.getInt(key, 0)); break;
                    case LONG: dstObj.putLong(key, srcObj.getLong(key, 0)); break;
                    case SHORT: dstObj.putShort(key, srcObj.getShort(key, (short) 0)); break;
                    case BYTE: dstObj.putByte(key, srcObj.getByte(key, (byte) 0)); break;
                    case FLOAT: dstObj.putFloat(key, srcObj.getFloat(key, 0)); break;
                    case DOUBLE: dstObj.putDouble(key, srcObj.getDouble(key, 0)); break;
                    case BOOLEAN: dstObj.putBoolean(key, srcObj.getBoolean(key, false)); break;
                    case STRING: dstObj.putString(key, srcObj.getString(key, "")); break;
                    default: throw new IllegalStateException("unsupported type " + type);
                }
            }
        } else if (src.isArray()) {
            IArrayReader srcArr = src.asArray();
            IArrayWriter dstArr = dst.asArray();
            boolean anySupported = srcArr.isGetAnySupported() && dstArr.isPutAnySupported();
            int size = srcArr.size();
            for (int i = 0; i < size; i++) {
                ESerializedType type = srcArr.getValueType(i);
                if (type.isScope()) {
                    readerToWriter(srcArr.beginScope(i), dstArr.beginScope());
                    continue;
                }
                if (anySupported) {
                    dstArr.addAny(srcArr.getAny(i, null));
                    continue;
                }
                switch (type) {
                    case INT: dstArr.addInt(srcArr.getInt(i, 0)); break;
                    case LONG: dstArr.addLong(srcArr.getLong(i, 0)); break;
                    case SHORT: dstArr.addShort(srcArr.getShort(i, (short) 0)); break;
                    case BYTE: dstArr.addByte(srcArr.getByte(i, (byte) 0)); break;
                    case FLOAT: dstArr.addFloat(srcArr.getFloat(i, 0)); break;
                    case DOUBLE: dstArr.addDouble(srcArr.getDouble(i, 0)); break;
                    case BOOLEAN: dstArr.addBoolean(srcArr.getBoolean(i, false)); break;
                    case STRING: dstArr.addString(srcArr.getString(i, "")); break;
                    default: throw new IllegalStateException("unsupported type " + type);
                }
            }
        }
        src.endScope();
        dst.endScope();
    }
}
