package com.reider745.instrument;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.utils.BinaryStream;
import com.reider745.api.ReflectHelper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class BlockDump {
    private record BlockClassInfo(String className, int blockId, List<String> methods) {
    }

    public static Set<Method> getDeclaredAndOverriddenMethods(Class<?> clazz) {

        // 1. Получаем все объявленные методы текущего класса
        Set<Method> declaredMethods = new HashSet<>(Arrays.asList(clazz.getDeclaredMethods()));

        // 2. Получаем все унаследованные методы из суперклассов
        Set<Method> inheritedMethods = new HashSet<>();
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null){
            inheritedMethods.addAll(Arrays.asList(superClass.getDeclaredMethods()));
            superClass = superClass.getSuperclass();
        }

        // 3. Определяем переопределенные методы
        Set<Method> overriddenMethods = inheritedMethods.stream()
                .filter(inheritedMethod -> Arrays.stream(clazz.getDeclaredMethods()).anyMatch(declaredMethod ->
                        declaredMethod.getName().equals(inheritedMethod.getName()) &&
                                Arrays.equals(declaredMethod.getParameterTypes(), inheritedMethod.getParameterTypes())
                )).collect(Collectors.toSet());

        //4. Объединяем объявленные методы и переопределенные методы
        declaredMethods.addAll(overriddenMethods);
        return declaredMethods;
    }

    public static void preInit(String mainClass, String[] args) {
        Callback.addCallback("PostLoaded", new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                final Class<?>[] blocks = ReflectHelper.getField(Block.class, "list");
                final List<BlockClassInfo> patch = new ArrayList<>();

                for (int id = 0; id < blocks.length; id++) {
                    final Class<?> clazz = blocks[id];

                    if (clazz != null && id < 8000) {
                        final List<String> methods = new ArrayList<>();

                        for(Method method : getDeclaredAndOverriddenMethods(clazz)) {
                            switch (method.getName()) {
                                case "isSolid":
                                case "getHardness":
                                case "getResistance":
                                case "getFrictionFactor":
                                case "getLightLevel":
                                    methods.add(method.getName());
                            }
                        }

                        if(!methods.isEmpty())
                            patch.add(new BlockClassInfo(clazz.getName(), id, methods));
                    }
                }


                final BinaryStream stream = new BinaryStream();
                stream.putShort(patch.size());

                for(BlockClassInfo info : patch) {
                    stream.putString(info.className);
                    stream.putByte((byte) info.methods.size());

                    for(String method : info.methods)
                        stream.putString(method);
                }

                try {
                    Files.write(new File("blocksDump.bin").toPath(), stream.getBuffer());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Server.getInstance().shutdown();

                return null;
            }
        }, 0);
    }
}
