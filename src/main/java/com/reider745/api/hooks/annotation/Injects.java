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

    String[] signature() default "-1";
    TypeHook type_hook() default TypeHook.AUTO;

    String class_name() default "-1";
    String method() default "-1";
    ArgumentTypes arguments_map() default ArgumentTypes.AUTO;

}
