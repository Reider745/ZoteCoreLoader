package org.innercore.icstd.serialization;

public interface IArrayReader {
    int size();

    ESerializedType getValueType(int idx);
    int getInt(int idx, int fallback);
    long getLong(int idx, long fallback);
    short getShort(int idx, short fallback);
    byte getByte(int idx, byte fallback);
    float getFloat(int idx, float fallback);
    double getDouble(int idx, double fallback);
    boolean getBoolean(int idx, boolean fallback);
    String getString(int idx, String fallback);

    boolean isGetAnySupported();
    Object getAny(int idx, Object fallback);

    IScopedReader beginScope(int idx);
    void endChildScope();
    void endScope();
}
