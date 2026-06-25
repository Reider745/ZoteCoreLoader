package org.innercore.icstd.serialization;

import java.util.Iterator;

public interface IObjectReader {
    boolean hasKey(String key);
    ESerializedType getValueType(String key);
    int getInt(String key, int fallback);
    long getLong(String key, long fallback);
    short getShort(String key, short fallback);
    byte getByte(String key, byte fallback);
    float getFloat(String key, float fallback);
    double getDouble(String key, double fallback);
    boolean getBoolean(String key, boolean fallback);
    String getString(String key, String fallback);

    boolean isGetAnySupported();
    Object getAny(String key, Object fallback);
    Iterable<String> iterKeys();

    IScopedReader beginScope(String key);
    void endChildScope();
    void endScope();
}
