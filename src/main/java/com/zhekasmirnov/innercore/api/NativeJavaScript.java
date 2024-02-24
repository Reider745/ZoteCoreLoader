package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Deprecated(since = "Zote")
public class NativeJavaScript {
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
            Logger.error("NativeJavaScript",
                    "invalid parameter passed for key " + key + ": " + value + ", it will be replaced with zero byte");
            stream.writeByte(TYPE_BYTE);
            stream.writeByte(0);
        }
    }

    private static int parseComplexParameters(DataOutputStream stream, ScriptableObject scriptable, String prefix)
            throws IOException {
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
            case 'O': // object
                return null;
            case 'I':
                return 0L;
            case 'F':
                return 0.0d;
            case 'S':
                return "";
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
                switch (params.charAt(i)) {
                    case 'B':
                    case 'C':
                        while ((argsSize + 4) % 4 != 0)
                            argsSize++;
                        argsSize += 1;
                        break;
                    case 'I':
                    case 'F':
                        while ((argsSize + 4) % 4 != 0)
                            argsSize++;
                        argsSize += 4;
                        break;
                    case 'L':
                    case 'D':
                        while ((argsSize + 4) % 8 != 0)
                            argsSize++;
                        argsSize += 8;
                        break;
                }
            }
            this.argsSize = argsSize;
        }

        public Object call(Context ctx, Scriptable scope1, Scriptable scope2, Object[] args) {
            return unwrapResult(0, signature);
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
                throw new IllegalArgumentException("complex native function " + module + "::" + name + " => "
                        + signature + " must receive exactly one javascript object as parameter");
            }
            return unwrapResult(0, signature);
        }
    }

    public static Callable getFunction(String module, String name) {
        return new BasicCaller(0, module, name, "V()");
    }

    public static boolean injectNativeModule(String module, ScriptableObject scope) {
        return false;
    }

    public static ScriptableObject wrapNativeModule(String module) {
        return new ScriptableObject() {
            @Override
            public String getClassName() {
                return "NativeJSModule_" + module;
            }

            @Override
            public Object get(String name, Scriptable start) {
                return new ScriptableFunctionImpl() {
                    @Override
                    public String getClassName() {
                        return "NativeJSModule_" + name;
                    }

                    @Override
                    public Scriptable getParentScope() {
                        return start;
                    }

                    @Override
                    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                        return wrapNativeModule(module);
                    }
                };
            }
        };
    }
}
