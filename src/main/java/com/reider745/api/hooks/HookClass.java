package com.reider745.api.hooks;


import javassist.CtClass;
import javassist.CtField;

public interface HookClass {
    default void rebuildField(CtClass ctClass, CtField field)  {};
}
