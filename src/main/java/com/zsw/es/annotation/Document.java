package com.zsw.es.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Document {

    String indexName() default "";

    String type() default "";

    int shards() default 3;

    int replicas() default 1;

    boolean createIndex() default false;

}
