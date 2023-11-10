package com.reider745.api.hooks.annotation;


import com.reider745.api.hooks.TypeHook;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface AutoInject {
    String method() default "-1";
    String signature() default "-1";
    String[] arguments() default {};
    TypeHook type_hook() default TypeHook.AUTO;

    boolean static_method() default true;
    String class_name() default "-1";
}
