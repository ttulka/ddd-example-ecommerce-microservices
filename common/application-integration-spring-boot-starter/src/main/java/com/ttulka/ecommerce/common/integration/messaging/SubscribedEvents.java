package com.ttulka.ecommerce.common.integration.messaging;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;

import com.ttulka.ecommerce.common.events.DomainEvent;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toSet;

/**
 * Finds the {@code EventListener}-annotated methods that listens to {@code DomainEvent} events.
 */
@RequiredArgsConstructor
class SubscribedEvents {

    private final @NonNull ApplicationContext context;
    private final @NonNull ConfigurableListableBeanFactory beanFactory;

    public Set<Class<?>> classes() {
        return classesAnnotatedWithComponent()
                .flatMap(this::methodsAnnotatedWithEventListener)
                .flatMap(this::eventClassesFromMethodParameters)
                .collect(toSet());
    }

    private Stream<? extends Class<?>> classesAnnotatedWithComponent() {
        return Stream.of(context.getBeanNamesForAnnotation(Component.class))
                .map(bean -> AutoProxyUtils.determineTargetClass(beanFactory, bean))
                .filter(c -> !isSpringContainerClass(c));
    }

    private Stream<Method> methodsAnnotatedWithEventListener(Class<?> clazz) {
        return MethodIntrospector.selectMethods(
                clazz,
                (MethodIntrospector.MetadataLookup<EventListener>) method ->
                        AnnotatedElementUtils.findMergedAnnotation(method, EventListener.class))
                .keySet().stream();
    }

    private Stream<Class<?>> eventClassesFromMethodParameters(Method method) {
        return Stream.of(method.getParameterTypes())
                .filter(DomainEvent.class::isAssignableFrom);
    }

    private boolean isSpringContainerClass(Class<?> clazz) {
        return clazz.getName().startsWith("org.springframework.");
    }
}
