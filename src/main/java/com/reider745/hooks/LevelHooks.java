package com.reider745.hooks;

import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;

@Hooks
public class LevelHooks implements HookClass {
    @Override
    public void rebuildField(CtClass ctClass, CtField field) {
        if(field.getName().equals("randomTickBlocks"))
            field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
    }
}
