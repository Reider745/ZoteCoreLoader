package com.reider745.api.hooks.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Hooks {
    String class_name() default "";
}
