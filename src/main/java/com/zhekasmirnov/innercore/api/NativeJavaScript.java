package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class NativeJavaScript {
    private static native String[] getFunctionListForModule(String module);
    private static native long getFunctionHandle(String module, String name);
    private static native int getFunctionCallType(long handle);
    private static native String getFunctionSignature(long handle);
    private static native long invokeBasicParameterFunction(long handle, byte[] buffer);
    private static native long invokeComplexParameterFunction(long handle, byte[] buffer);

    private static native long unwrapLongResult(long result);
    private static native double unwrapDoubleResult(long result);
    private static native String unwrapStringResult(long result);
    private static native Object unwrapObjectResult(long result);

    private static final int CALL_TYPE_BASIC = 1;
    private static final int CALL_TYPE_COMPLEX = 2;

    private static final int TYPE_BYTE = 0;
    private static final int TYPE_INT = 1;
    private static final int TYPE_LONG = 2;
    private static final int TYPE_DOUBLE = 3;
    private static final int TYPE_STRING = 4;

    private static void writeValue(DataOutputStream stream, String key, Object value) throws IOException {
        stream.writeUTF(key);
        if (value instanceof Number) {
            Number number = (Number) value;
            if (number instanceof Long) {
                stream.writeByte(TYPE_LONG);
                stream.writeLong(number.longValue());
            } else if (number instanceof Double || number instanceof Float) {
                stream.writeByte(TYPE_DOUBLE);
                stream.writeDouble(number.doubleValue());
            } else if (number instanceof Byte) {
                stream.writeByte(TYPE_BYTE);
                stream.writeByte(number.byteValue());
            } else {
                stream.writeByte(TYPE_INT);
                stream.writeInt(number.intValue());
            }
        } else if (value instanceof CharSequence) {
            stream.writeByte(TYPE_STRING);
            stream.writeUTF(value.toString());
        } else {
            Logger.error("NativeJavaScript", "invalid parameter passed for key " + key + ": " + value + ", it will be replaced with zero byte");
            stream.writeByte(TYPE_BYTE);
            stream.writeByte(0);
        }
    }

    private static int parseComplexParameters(DataOutputStream stream, ScriptableObject scriptable, String prefix) throws IOException {
        int count = 0;
        Object[] ids = scriptable.getAllIds();
        for (Object key : ids) {
            Object value = scriptable.get(key);
            if (value instanceof ScriptableObject) {
                count += parseComplexParameters(stream, (ScriptableObject) value, prefix + key.toString() + ".");
            } else {
                writeValue(stream, prefix + key.toString(), value);
                count++;
            }
        }
        return count;
    }

    public static byte[] parseComplexParameters(ScriptableObject parameters) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(bos);
            stream.writeInt(-1);
            int count = parseComplexParameters(stream, parameters, "");
            byte[] bytes = bos.toByteArray();
            bytes[0] = (byte) ((count >>> 24) & 0xFF);
            bytes[1] = (byte) ((count >>> 16) & 0xFF);
            bytes[2] = (byte) ((count >>> 8) & 0xFF);
            bytes[3] = (byte) ((count >>> 0) & 0xFF);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object unwrapResult(long result, String signature) {
        if (signature == null || signature.length() == 0) {
            throw new RuntimeException("invalid native caller signature: " + signature);
        }

        switch (signature.charAt(0)) {
            case 'V': // void
            return null;
            case 'I':
            return unwrapLongResult(result);
            case 'F':
            return unwrapDoubleResult(result);
            case 'S':
            return unwrapStringResult(result);
            case 'O':
            return unwrapObjectResult(result);
            default:
            throw new RuntimeException("invalid native function signature: " + signature);
        }
    }

    public static class BasicCaller extends ScriptableFunctionImpl {
        public final long handle;
        public final String module, name, signature, params;
        public final int argsSize;

        public BasicCaller(long handle, String module, String name, String signature) {
            this.handle = handle;
            this.module = module;
            this.name = name;
            this.signature = signature;
            int begin = signature.indexOf('(') + 1;
            int end = signature.indexOf(')');
            if (begin == 0 || end == -1) {
                throw new RuntimeException("invalid native function signature: " + signature);
            }
            this.params = signature.substring(begin, end);
            
            int argsSize = 0;
            for (int i = 0; i < params.length(); i++) {
                switch(params.charAt(i)) {
                    case 'B':
                    case 'C':
                        while ((argsSize + 4) % 4 != 0) argsSize++;
                        argsSize += 1;
                        break;
                    case 'I':
                    case 'F':
                        while ((argsSize + 4) % 4 != 0) argsSize++;
                        argsSize += 4;
                        break;
                    case 'L':
                    case 'D':
                        while ((argsSize + 4) % 8 != 0) argsSize++;
                        argsSize += 8;
                        break;
                };
            };
            this.argsSize = argsSize;
        }

        public Object call(Context ctx, Scriptable scope1, Scriptable scope2, Object[] args) {
            byte[] argsBytes = new byte[argsSize];
            int index = 0;

            for (int i = 0; i < args.length; i++) {
                if (i >= params.length()) {
                    ICLog.d("NativeJavaScript", "native function " + module + ":" + name + " have excess parameters passed");
                    break;
                }
                int iBits;
                long lBits;
                switch (params.charAt(i)) {
                    case 'C':
                    while ((index + 4) % 4 != 0) argsBytes[index++] = 0;
                    argsBytes[index++] = ((Number) args[i]).byteValue();
                    break;
                    case 'B':
                    while ((index + 4) % 4 != 0) argsBytes[index++] = 0;
                    argsBytes[index++] = Context.toBoolean(args[i]) ? (byte) 1 : (byte) 0;
                    break;
                    case 'I':
                    while ((index + 4) % 4 != 0) argsBytes[index++] = 0;
                    iBits = ((Number) args[i]).intValue();
                    argsBytes[index++] = (byte) ((iBits >>> 0) & 0xFF);
                    argsBytes[index++] = (byte) ((iBits >>> 8) & 0xFF);
                    argsBytes[index++] = (byte) ((iBits >>> 16) & 0xFF);
                    argsBytes[index++] = (byte) ((iBits >>> 24) & 0xFF);
                    break;
                    case 'F':
                    while ((index + 4) % 4 != 0) argsBytes[index++] = 0;
                    iBits = Float.floatToIntBits(((Number) args[i]).floatValue());
                    argsBytes[index++] = (byte) ((iBits >>> 0) & 0xFF);
                    argsBytes[index++] = (byte) ((iBits >>> 8) & 0xFF);
                    argsBytes[index++] = (byte) ((iBits >>> 16) & 0xFF);
                    argsBytes[index++] = (byte) ((iBits >>> 24) & 0xFF);
                    break;
                    case 'L':
                    while ((index + 4) % 8 != 0) argsBytes[index++] = 0;
                    lBits = ((Number) Context.jsToJava(args[i], Number.class)).longValue();
                    argsBytes[index++] = (byte) ((lBits >>> 0L) & 0xFFL);
                    argsBytes[index++] = (byte) ((lBits >>> 8L) & 0xFFL);
                    argsBytes[index++] = (byte) ((lBits >>> 16L) & 0xFFL);
                    argsBytes[index++] = (byte) ((lBits >>> 24L) & 0xFFL);
                    argsBytes[index++] = (byte) ((lBits >>> 32L) & 0xFFL);
                    argsBytes[index++] = (byte) ((lBits >>> 40L) & 0xFFL);
                    argsBytes[index++] = (byte) ((lBits >>> 48L) & 0xFFL);
                    argsBytes[index++] = (byte) ((lBits >>> 56L) & 0xFFL);
                    break;
                    case 'D':
                    while ((index + 4) % 8 != 0) argsBytes[index++] = 0;
                    lBits = Double.doubleToLongBits(((Number) Context.jsToJava(args[i], Number.class)).doubleValue());
                    argsBytes[index++] = (byte) ((lBits >>> 0) & 0xFF);
                    argsBytes[index++] = (byte) ((lBits >>> 8) & 0xFF);
                    argsBytes[index++] = (byte) ((lBits >>> 16) & 0xFF);
                    argsBytes[index++] = (byte) ((lBits >>> 24) & 0xFF);
                    argsBytes[index++] = (byte) ((lBits >>> 32) & 0xFF);
                    argsBytes[index++] = (byte) ((lBits >>> 40) & 0xFF);
                    argsBytes[index++] = (byte) ((lBits >>> 48) & 0xFF);
                    argsBytes[index++] = (byte) ((lBits >>> 56) & 0xFF);
                    break;
                }
            }

            long result = invokeBasicParameterFunction(handle, argsBytes);
            return unwrapResult(result, signature);
        }
    }

    public static class ComplexCaller extends ScriptableFunctionImpl {
        public final long handle;
        public final String module, name, signature;

        public ComplexCaller(long handle, String module, String name, String signature) {
            this.handle = handle;
            this.module = module;
            this.name = name;
            this.signature = signature;
        }

        public Object call(Context ctx, Scriptable scope1, Scriptable scope2, Object[] args) {
            if (args.length != 1 || !(args[0] instanceof ScriptableObject)) {
                throw new IllegalArgumentException("complex native function " + module + "::" + name + " => " + signature + " must receive exactly one javascript object as parameter");
            }
            long result = invokeComplexParameterFunction(handle, parseComplexParameters((ScriptableObject) args[0]));
            return unwrapResult(result, signature);
        }
    }

    public static Callable getFunction(String module, String name) {
        long handle = getFunctionHandle(module, name);
        if (handle != 0) {
            String signature = getFunctionSignature(handle);
            switch (getFunctionCallType(handle)) {
                case CALL_TYPE_BASIC:
                    return new BasicCaller(handle, module, name, signature);
                case CALL_TYPE_COMPLEX:
                    return new ComplexCaller(handle, module, name, signature);
            }
        }
        Logger.error("NativeJavaScript", "failed to wrap native function " + module + "::" + name + " for some reason");
        return null;
    }
    
    public static boolean injectNativeModule(String module, ScriptableObject scope) {
        String[] functions = getFunctionListForModule(module);
        if (functions != null && functions.length > 0) {
            for (String functionName : functions) {
                Callable function = getFunction(module, functionName);
                if (function != null) {
                    scope.put(functionName, scope, function);
                }
            }
            return true;
        }
        Logger.error("NativeJavaScript", "failed to import native module: " + module);
        return false;
    }

    public static ScriptableObject wrapNativeModule(String module) {
        ScriptableObject scope = new ScriptableObject() {
            @Override
            public String getClassName() {
                return "NativeJSModule_" + module;
            }
        };
        if (injectNativeModule(module, scope)) {
            return scope;
        } else {
            return null;
        }
    };


};
