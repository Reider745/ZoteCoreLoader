package com.reider745.api.hooks.annotation;

import com.reider745.api.hooks.ArgumentTypes;
import com.reider745.api.hooks.TypeHook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Injects {
    String[] signature() default "";
    TypeHook type() default TypeHook.AUTO;
    String className() default "";
    String method() default "";
    ArgumentTypes argumentMap() default ArgumentTypes.AUTO;
}
