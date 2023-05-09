package com.zhekasmirnov.apparatus.cpp;

import com.zhekasmirnov.innercore.api.NativeAPI;
import org.json.JSONObject;

import java.util.Iterator;

public class NativeIdPlaceholderGenerator {
    private static native void addItemPlaceholder(int id, String nameId);
    private static native void addBlockPlaceholder(int id, String nameId);

    private static native void setPlaceholderItemTexture(String name, int id);
    private static native void setPlaceholderBlockTexture(String name, int id);


    public static native void clearAll();

    public static void addItem(int id, String nameId) {
        addItemPlaceholder(id, NativeAPI.convertNameId(nameId));
    }

    public static void addBlock(int id, String nameId) {
        addBlockPlaceholder(id, NativeAPI.convertNameId(nameId));
    }

    public static void rebuildFromServerPacket(JSONObject packet) {
        clearAll();
        for (Iterator<String> it = packet.keys(); it.hasNext(); ) {
            String key = it.next();
            int id = packet.optInt(key);
            if (id != 0) {
                if (key.startsWith("block:")) {
                    addBlock(id, key.substring(6));
                } else if (key.startsWith("item:")) {
                    addItem(id, key.substring(5));
                }
            }
        }
    }
}
