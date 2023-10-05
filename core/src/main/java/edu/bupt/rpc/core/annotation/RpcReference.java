package edu.bupt.rpc.core.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface RpcReference {
    String group() default "";
    String version() default "";
}
