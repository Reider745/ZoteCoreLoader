package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;
import com.reider745.api.ClientOnly;

@ClientOnly
public class NativeOptions {
    protected static long getOptionByName(String name){
        InnerCoreServer.useClientMethod("NativeOptions.getOptionByName");
        return 0;
    }

    protected static int getType(long ptr){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
        return OptionType.NONE;
    }

    protected static float getFloat(long ptr){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
       return 0;
    }
    protected static String getString(long ptr){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
        return "";
    }
    protected static int getInt(long ptr){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
        return 0;
    }
    protected static boolean getBool(long ptr){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
        return false;
    }

    protected static float getFloatMin(long ptr){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
        return 0;
    }
    protected static float getFloatMax(long ptr){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
        return 0;
    }

    protected static int getIntMin(long ptr){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
        return 0;
    }
    protected static int getIntMax(long ptr){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
        return 0;
    }

    protected static boolean canModify(long ptr){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
        return false;
    }

    public static String[] getOptions(){
        InnerCoreServer.useClientMethod("NativeOptions.getType");
        return new String[]{};
    }

    public static class OptionType {
        public static final int NONE = 0;
        public static final int BOOLEAN = 1;
        public static final int FLOAT = 2;
        public static final int FLOAT_SLIDER = 3;
        public static final int STRING = 4;
        public static final int INT = 5;
        public static final int INT_SLIDER = 6;
    }

    public static class Option {
        private final long ptr;

        protected Option(long ptr) {
            this.ptr = ptr;
        }

        public int getType(){
            return NativeOptions.getType(ptr);
        }

        public boolean canModify(){
            return NativeOptions.canModify(ptr);
        }

        public long getPointer() {
            return ptr;
        }

        public float getFloat(){
            return NativeOptions.getFloat(ptr);
        }

        public String getString(){
            return NativeOptions.getString(ptr);
        }

        public int getInt(){
            return NativeOptions.getInt(ptr);
        }

        public boolean getBoolean(){
            return NativeOptions.getBool(ptr);
        }

        public float getFloatMin(){
            return NativeOptions.getFloatMin(ptr);
        }

        public float getFloatMax(){
            return NativeOptions.getFloatMax(ptr);
        }

        public int getIntMin(){
            return NativeOptions.getIntMin(ptr);
        }

        public int getIntMax(){
            return NativeOptions.getIntMax(ptr);
        }
    }

    public static Option get(String name){
        long ptr = getOptionByName(name);
        if(ptr != -1)
            return new Option(ptr);
        return null;
    }
}