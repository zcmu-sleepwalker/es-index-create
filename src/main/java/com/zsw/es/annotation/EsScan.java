package com.zsw.es.annotation;

import com.zsw.es.service.ImportEsIndexCreatorRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Import(ImportEsIndexCreatorRegistrar.class)
public @interface EsScan {
	String[] value() default {};
}
