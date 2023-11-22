package com.reider745.api.hooks;

import javassist.CtClass;
import javassist.Modifier;

import javassist.*;
import javassist.bytecode.*;

public class CtClassEditor {
    public static final int Static = Modifier.STATIC;
    public static final int Public = Modifier.PUBLIC;
    public static final int Private = Modifier.PRIVATE;
    public static final int PublicStatic = Public | Static;
    public static final int PrivateStatic = Private | Static;

    private final CtClass ctClass;

    public CtClassEditor(CtClass ctClass){
        this.ctClass = ctClass;
    }

    public final CtClass getCtClass() {
        return ctClass;
    }

    public final CtField addField(Class<?> type, String name, int modifier, String src){
        try{
            src += ";";

            ClassPool pool = ClassPool.getDefault();
            CtField field = new CtField(pool.get(type.getName()), name, ctClass);
            field.setModifiers(modifier);
            ctClass.addField(field, src);
            return field;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
