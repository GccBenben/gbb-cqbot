package com.gccbenben.qqbotservice.component.messageMethodHandleStrategy;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Action {
    String name() default "";
}
