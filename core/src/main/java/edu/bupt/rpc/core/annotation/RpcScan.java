package edu.bupt.rpc.core.annotation;

import edu.bupt.rpc.core.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Import(CustomScannerRegistrar.class)
public @interface RpcScan {
    String[] basePackage();
}
