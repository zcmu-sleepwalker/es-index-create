package com.zsw.es.service;

import com.zsw.es.annotation.Document;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.Set;


public class EsScaner extends ClassPathBeanDefinitionScanner {
	public EsScaner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
		super(registry, useDefaultFilters);
	}
	@Override
	protected void registerDefaultFilters() {
		TypeFilter typeFilter = new TypeFilter() {
			@Override
			public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
				Set<String> annotationTypes = metadataReader.getAnnotationMetadata().getAnnotationTypes();
				if (annotationTypes.contains(Document.class.getName())){
					return true;
				}
				return false;
			}
		};
		addIncludeFilter(typeFilter);
	}

	public boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition){
		return beanDefinition.getMetadata().isConcrete();
	}

	@Override
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		return  super.doScan(basePackages);
	}
}
