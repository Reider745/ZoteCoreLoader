package com.reider745;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.reider745.api.hooks.JarEditor;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.LineNumberAttribute;

public class Main {
    public static final JarEditor loader = new JarEditor();

    public static void add(String name) throws ClassNotFoundException, NotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        try {
            loader.registerHooksInitializationForClass(name);
        } catch (Exception e) {
            System.out.print("registerHooksInitializationForClass(" + name + "): ");
            e.printStackTrace();
        }
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

    public static void main(String[] args) throws Throwable {
        if (Arrays.stream(args).anyMatch("--snowfall-everywhere"::equals)) {
            add("com.reider745.hooks.SnowfallEverywhere");
        }

        //items and blocks
        add("com.reider745.hooks.block.BlockPalette");
        add("com.reider745.hooks.item.ItemUtils");
        add("com.reider745.hooks.item.RuntimeItemsHooks");
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
        add("com.reider745.hooks.LevelHooks");
        loader.addRebuildField("com.reider745.hooks.LevelHooks", (ctClass, field) -> {
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


        loader.init(Arrays.stream(args).anyMatch("--debug-hooks"::equals));
        loader.run("cn.nukkit.Nukkit", args);
    }
}