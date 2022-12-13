package com.zsw.es.check;

import com.alibaba.fastjson.JSON;
import com.zsw.es.annotation.Document;
import com.zsw.es.annotation.Field;
import com.zsw.es.constant.FieldType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class EsIndexCheck {

    Set<BeanDefinitionHolder> beanDefinitionHolders = new HashSet<>();

    public EsIndexCheck(Set<BeanDefinitionHolder> beanDefinitionHolders) {
        this.beanDefinitionHolders = beanDefinitionHolders;
    }

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @SneakyThrows
    @PostConstruct
    public void init() {
        try {
            for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
                String beanClassName = beanDefinitionHolder.getBeanDefinition().getBeanClassName();
                Class<?> clazz = Class.forName(beanClassName);
                Document annotation = clazz.getAnnotation(Document.class);
                String indexName = annotation.indexName();
                String type = annotation.type();
                int replicas = annotation.replicas();
                int shards = annotation.shards();
                boolean createIndex = annotation.createIndex();
                if (!createIndex) {
                    continue;
                }

                Settings settings = Settings.builder()
                        .put("index.number_of_shards", shards)
                        .put("index.number_of_replicas", replicas).build();

                boolean ifExist = this.existIndex(indexName);

                CreateIndexRequest request = new CreateIndexRequest(indexName, settings);

                Map<String, Object> properties = new HashMap<>();

                java.lang.reflect.Field[] declaredFields = clazz.getDeclaredFields();

                for (java.lang.reflect.Field field : declaredFields) {
                    String name = field.getName();
                    Field file = field.getAnnotation(Field.class);
                    if (file == null) {
                        continue;
                    }
                    String value = file.type();
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", value.toLowerCase());
                    if (FieldType.Nested.equals(value)) {
                        handleNestedMapping(field, map);
                    }

                    properties.put(name, map);
                }

                Map<String, Object> mapping = new HashMap<>();
                mapping.put("properties", properties);

                String s = JSON.toJSONString(mapping);
                request.mapping("_doc", s, XContentType.JSON);

                if (!ifExist) {
                    restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
                    log.info("es index [" + indexName + "] created");
                } else {
                    PutMappingRequest putMappingRequest = Requests.putMappingRequest(indexName)
                            .type("_doc")
                            .source(mapping);
                    restHighLevelClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean existIndex(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest().indices(index);
        //参数
        request.local(false);//从主节点返回本地索引信息状态
        request.humanReadable(true);//以适合人类的格式返回
        request.includeDefaults(false);//是否返回每个索引的所有默认配置

        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        return exists;
    }

    private void handleNestedMapping(java.lang.reflect.Field field, Map<String, Object> map) {
        field.setAccessible(true);
        Class<?> type = field.getType();
        if (List.class.getName().equals(type.getName())) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                type = (Class<?>) pt.getActualTypeArguments()[0];
            }
        }
        java.lang.reflect.Field[] internalFields = type.getDeclaredFields();
        Map<String, Object> internalProperties = new HashMap<>();
        for (java.lang.reflect.Field internalField : internalFields) {
            String name = internalField.getName();
            Field esField = internalField.getAnnotation(Field.class);
            if (esField == null) {
                continue;
            }
            String value = esField.type();
            Map<String, Object> internalMap = new HashMap<>();
            internalMap.put("type", value.toLowerCase());
            internalProperties.put(name, internalMap);
        }
        map.put("properties", internalProperties);
    }

}
