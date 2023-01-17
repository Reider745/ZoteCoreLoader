package com.reider745;

import android.util.Log;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InnerCoreServer {
    static {

    }

    private static long hash(int id, int data, int[] allStatesSorted) {
        long hash = (long) id;
        hash = hash * 314159L + (long) data;

        int state_index = 0;
        for (int state_value : allStatesSorted) {
            state_index++;
            hash = hash * 314159L + (long) (state_value | (state_index << 5));
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
        public RuntimeId add(int id, int data, int[] states, int runtimeId){
            return add(hash(id, data, states), runtimeId);
        }
        public byte[] getRuntimeIds(){
            ByteBuffer buffer = ByteBuffer.allocate(list.size()*SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            for (Entry entry : list){
                buffer.putLong(entry.hash);
                buffer.putInt(entry.runtimeId);
                //if(!entry.end)
                    buffer.putInt(0);
            }
            return buffer.array();
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
