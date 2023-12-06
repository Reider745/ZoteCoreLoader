package com.reider745.api.hooks.annotation;


import com.reider745.api.hooks.ArgumentTypes;
import com.reider745.api.hooks.TypeHook;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Inject {
    String method() default "";
    String signature() default "";
    TypeHook type() default TypeHook.AUTO;
    String className() default "";
    ArgumentTypes argumentMap() default ArgumentTypes.AUTO;
}
