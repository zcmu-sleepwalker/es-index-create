package com.zsw.es.service;

import com.zsw.es.annotation.EsScan;
import com.zsw.es.check.EsIndexCheck;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Set;

public class ImportEsIndexCreatorRegistrar implements ImportBeanDefinitionRegistrar {

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EsScan.class.getName());
        Set<BeanDefinitionHolder> beanDefinitionHolders = scan(registry, annotationAttributes);
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(EsIndexCheck.class);
        beanDefinitionBuilder.addConstructorArgValue(beanDefinitionHolders);
        registry.registerBeanDefinition("esIndexCheck", beanDefinitionBuilder.getBeanDefinition());
    }

    public Set<BeanDefinitionHolder> scan(BeanDefinitionRegistry registry, Map<String, Object> annotationAttributes) {
        String[] scanPackageName = (String[]) annotationAttributes.get("value");
        EsScaner scaner = new EsScaner(registry, true);
        return scaner.doScan(scanPackageName);
    }

}
