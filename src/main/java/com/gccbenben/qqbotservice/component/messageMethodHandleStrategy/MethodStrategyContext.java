package com.gccbenben.qqbotservice.component.messageMethodHandleStrategy;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MethodStrategyContext {
    private static Map<String, Class> allActions;
    private static final String PACKAGE_NAME = "com.gccbenben.qqbotservice";

    static  {
        Reflections reflections = new Reflections(PACKAGE_NAME);
        Set<Class<?>> annotationClasses = reflections.getTypesAnnotatedWith(Action.class);
        allActions = new ConcurrentHashMap<>();
        for (Class<?> classObject: annotationClasses) {
            Action action = classObject.getAnnotation(Action.class);
            allActions.put(action.name(), classObject);
        }
        allActions = Collections.unmodifiableMap(allActions);
    }

    private IMethodHandleStrategy methodHandleStrategy;

    /**
     * 设置策略接口
     */
    public MethodStrategyContext(String name) {
        if (allActions.containsKey(name)) {
            log.info("Created Action name is {}", name);
            try {
                methodHandleStrategy = (IMethodHandleStrategy)allActions.get(name).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("Instantiate Action failed", e);
            }
        } else {
            log.warn("Specified Action name {} does not exist", name);
        }
    }

    public String handleMethod(ObjectNode params, String actionKeyword) {
        if (methodHandleStrategy != null) {
            return methodHandleStrategy.handleMethod(params);
        } else {
            log.warn("未检索到对应动作关键字，请检查关键字：{}", actionKeyword);
        }
        return null;
    }
}
