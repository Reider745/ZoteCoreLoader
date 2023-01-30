package com.reider745;

import android.util.Log;
import cn.nukkit.Server;
import com.google.common.io.ByteStreams;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InnerCoreServer {
    static {

    }

    public static long hash(int id, int data, int[][] allStatesSorted) {
        long hash = (long) id;
        hash = hash * 314159L + (long) data;

        for (int[] state : allStatesSorted) {
            hash = hash * 314159L + (long) (state[1] | ((state[0] + 1) << 5));
        }
        return hash;
    }

    public static class RuntimeId {
        private static final int SIZE = 16;

        private static class Entry {
            long hash;
            int runtimeId;

            boolean end;
        }

        private ArrayList<Entry> list = new ArrayList<>();

        public RuntimeId add(long hash, int runtimeId){
            if(list.size() > 0) list.get(list.size()-1).end = false;
            Entry entry = new Entry();
            entry.hash = hash;
            entry.runtimeId = runtimeId;
            entry.end = true;
            list.add(entry);
            return this;
        }
        public RuntimeId add(int id, int data, int[][] states, int runtimeId){
            return add(hash(id, data, states), runtimeId);
        }
        public byte[] getRuntimeIds(){
            return null;
        }
       /* private ByteBuffer buffer;

        private int capacity = SIZE;
        public RuntimeId(){
            buffer = ByteBuffer.allocate(SIZE);
        }

        private void resize(){
            capacity += SIZE;
            ByteBuffer buffer = ByteBuffer.allocate(capacity);
            for(int i = 0;i < capacity;i++)
                buffer.put(buffer.get(i));
            this.buffer = buffer;
        }

        public RuntimeId add(long hash, int runtimeId){
            buffer.putLong(hash);
            buffer.putInt(runtimeId);
            buffer.putInt(0);
            resize();
            return this;
        }
        public RuntimeId add(int id, int data, int[] states, int runtimeId){
            return add(hash(id, data, states), runtimeId);
        }
        public byte[] getRuntimeIds(){
            byte[] result = buffer.array();
            return result;
        }*/
    }
}
