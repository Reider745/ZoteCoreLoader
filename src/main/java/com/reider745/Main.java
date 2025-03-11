package com.reider745;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Objects;

import cn.nukkit.utils.BinaryStream;
import com.reider745.api.hooks.JarEditor;

import com.reider745.patch.PatchPreLoader;
import javassist.*;
import javassist.bytecode.LineNumberAttribute;

public class Main {
    public final PatchPreLoader patchLoader;
    public final JarEditor loader;

    public Main(String patchFolder) throws MalformedURLException {
        this.patchLoader = new PatchPreLoader(patchFolder);
        this.loader = new JarEditor(patchLoader.getPatchLoader());
    }



    public void add(String name) throws ClassNotFoundException, NotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        try {
            loader.registerHooksInitializationForClass(name);
        } catch (Exception e) {
            System.out.print("registerHooksInitializationForClass(" + name + "): ");
            e.printStackTrace();
        }
    }

    public void init(String[] args) throws NotFoundException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        if (Arrays.stream(args).anyMatch("--snowfall-everywhere"::equals)) {
            add("com.reider745.hooks.SnowfallEverywhere");
        }

        //items and blocks
        add("com.reider745.hooks.block.BlockPalette");
        add("com.reider745.hooks.item.ItemUtils");
        add("com.reider745.hooks.item.RuntimeItemsHooks");
        loader.addInit("com.reider745.hooks.block.BlocksHooks", ctClass -> {
            final ClassPool pool = loader.getClassPool();
            final BinaryStream stream = new BinaryStream(Main.class.getClassLoader().getResourceAsStream("blocksDump.bin").readAllBytes());

            final int countClass = stream.getShort();
            for(int i = 0;i < countClass;i++) {
                final CtClass classPatch = pool.get(stream.getString());
                final int countMethods = stream.getByte();

                for(int a = 0;a < countMethods;a++) {
                    final CtMethod methodPatch = classPatch.getDeclaredMethod(stream.getString());

                    if(methodPatch.getName().equals("isSolid")) {
                        methodPatch.insertBefore("Boolean val = com.reider745.hooks.block.BlocksHooks.isSolid(this);\n" +
                                "if(val != null)\n" +
                                "return val.booleanValue();");
                    }

                    if(methodPatch.getName().equals("getHardness")) {
                        methodPatch.insertBefore("Double val = com.reider745.hooks.block.BlocksHooks.getHardness(this);\n" +
                                "if(val != null)\n" +
                                "return val.doubleValue();");
                    }

                    if(methodPatch.getName().equals("getResistance")) {
                        methodPatch.insertBefore("Double val = com.reider745.hooks.block.BlocksHooks.getResistance(this);\n" +
                                "if(val != null)\n" +
                                "return val.doubleValue();");
                    }

                    if(methodPatch.getName().equals("getFrictionFactor")) {
                        methodPatch.insertBefore("Double val = com.reider745.hooks.block.BlocksHooks.getFrictionFactor(this);\n" +
                                "if(val != null)\n" +
                                "return val.doubleValue();");
                    }

                    if(methodPatch.getName().equals("getLightLevel")) {
                        methodPatch.insertBefore("Integer val = com.reider745.hooks.block.BlocksHooks.getLightLevel(this);\n" +
                                "if(val != null)\n" +
                                "return val.intValue();");
                    }
                }
            }
        });
        add("com.reider745.hooks.block.BlocksHooks");
        loader.addRebuildField("com.reider745.hooks.block.BlocksHooks", (ctClass, field) -> {
            String name = field.getName();
            if (name.equals("MAX_BLOCK_ID") || name.equals("usesFakeWater"))
                field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
        });
        add("com.reider745.event.DispenseBehaviorOverrides");
        loader.addInit("com.reider745.event.DispenseBehaviorOverrides", ctClass -> {
            try {

                CtMethod dispenseMethod = ctClass.getDeclaredMethod("dispense");
                dispenseMethod.insertAt(dispenseMethod.getMethodInfo().getLineNumber(310),
                        "com.reider745.event.DispenseBehaviorOverrides.dispenseSlot = slot;");
            } catch (NotFoundException | CannotCompileException e) {
                e.printStackTrace();
                System.out.println("Nukkit-MOT has been updated and overrides for `BlockDispenser.dispense` method are no longer available.");
            }
        });
        add("com.reider745.network.CraftingTransactionPacket");

        //entity
        add("com.reider745.hooks.entity.EntityOverrides");
        add("com.reider745.hooks.entity.PlayerHooks");

        //Js engine
        //add("com.reider745.jsengine.graalvm.hooks.ArgumentsPatch");
        add("com.reider745.hooks.RhinoOverrides");
        loader.addInit("com.reider745.hooks.RhinoOverrides", ctClass -> {
            try {
                CtMethod reflectMethod = ctClass.getDeclaredMethod("reflect");
                reflectMethod.insertAt(reflectMethod.getMethodInfo().getLineNumber(76),
                        "method = org.mozilla.javascript.MembersPatch.override(cl, method);");
            } catch (NotFoundException | CannotCompileException e) {
                e.printStackTrace();
                System.out.println("Rhino has been updated and overrides for `JavaMembers.reflect` method are no longer available.");
            }
        });

        //technical
        add("com.reider745.hooks.AndroidHooks");
        add("com.reider745.hooks.GlobalBanList");
        add("com.reider745.hooks.network.NetworkHooks");
        add("com.reider745.hooks.network.CraftingDataPacketHooks");
        add("com.reider745.hooks.world.LevelHooks");
        add("com.reider745.hooks.world.BiomesHooks");
        loader.addRebuildField("com.reider745.hooks.world.LevelHooks", (ctClass, field) -> {
            if (field.getName().equals("randomTickBlocks"))
                field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
        });
        add("com.reider745.hooks.ServerHooks");
        /*loader.addInit("com.reider745.hooks.ServerHooks", server -> {
            try {
                var ctClass = loader.get("org.graalvm.polyglot.AbstractValue");
                ctClass.getDeclaredField("context")
                        .setModifiers(Modifier.PUBLIC);

                ctClass = loader.get("com.oracle.truffle.polyglot.PolyglotMap");
                ctClass.getDeclaredField("languageContext")
                        .setModifiers(Modifier.PUBLIC);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });*/

        // bug fix
        //add("com.reider745.hooks.bugfix.DimensionsFix");

        patchLoader.init();

        loader.init(Arrays.stream(args).anyMatch("--debug-hooks"::equals));
        loader.run("cn.nukkit.Nukkit", args);
    }

    /*
    Не Загружать лишних классов, только патчи!
    Иначе классы могут загрузится в не нужном ClassLoader, что может привести к не предсказуемым последствиям
     */
    private static void printList(ClassLoader CL)  {
        Instrumentation inst = InstrumentHook.getInstrumentation();
        for (Class<?> clazz: inst.getAllLoadedClasses()) {
            System.err.println(clazz.getName());
        }
    }

    public static Main INSTANCE;

    public static void main(String[] args) throws Throwable {
        String folderPatches = "patches";

        for(String arg : args)
            if(arg.startsWith("--patches="))
                folderPatches = arg.replace("--patches=", "");

        INSTANCE = new Main(folderPatches);
        INSTANCE.init(args);
    }
}