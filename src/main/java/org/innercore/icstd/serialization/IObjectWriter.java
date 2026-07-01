package org.innercore.icstd.serialization;

public interface IObjectWriter {
    IObjectWriter putInt(String key, int value);
    IObjectWriter putLong(String key, long value);
    IObjectWriter putShort(String key, short value);
    IObjectWriter putByte(String key, byte value);
    IObjectWriter putFloat(String key, float value);
    IObjectWriter putDouble(String key, double value);
    IObjectWriter putBoolean(String key, boolean value);
    IObjectWriter putString(String key, String value);

    boolean isPutAnySupported();
    void putAny(String key, Object value);

    IScopedWriter beginScope(String key);
    void endScope();
}
