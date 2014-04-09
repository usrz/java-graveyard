/* ========================================================================== *
 * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
 * -------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *  http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ========================================================================== */
package org.usrz.libs.inject.utils;

import static org.usrz.libs.inject.utils.Parameters.notNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import org.usrz.libs.inject.Nullable;
import org.usrz.libs.inject.Optional;
import org.usrz.libs.inject.ProvidedBy;
import org.usrz.libs.inject.TypeException;
import org.usrz.libs.inject.TypeLiteral;

/**
 * Annotation utilities.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class Annotations {

    private Annotations() {
        throw new IllegalStateException("Do not construct");
    }

    /**
     * Returns <em>true</em> if the specified {@link Class} is annotated with
     * the {@link Singleton} annotation.
     */
    public static boolean isSingleton(Class<?> type) {
        return type.isAnnotationPresent(Singleton.class);
    }

    /**
     * Returns <em>true</em> if the specified {@link AnnotatedElement} is
     * annotated with the {@link Optional} annotation.
     */
    public static boolean isOptional(AnnotatedElement element) {
        return element.isAnnotationPresent(Optional.class);
    }

    /**
     * Returns <em>true</em> if the specified {@link AnnotatedElement} is
     * annotated with the {@link Inject} annotation.
     */
    public static boolean isInjectable(AnnotatedElement element) {
        notNull(element, "Null annotated element");
        return element.isAnnotationPresent(Inject.class);
    }

    /**
     * Returns <em>true</em> if the specified {@link AnnotatedElement} is
     * annotated with the {@link Nullable} annotation.
     */
    public static boolean isNullable(AnnotatedElement element) {
        if (element.isAnnotationPresent(Nullable.class)) {
            final Class<?> type;
            final Class<?> owner;
            if (element instanceof Parameter) {
                final Parameter parameter = ((Parameter) element);
                owner = parameter.getDeclaringExecutable().getDeclaringClass();
                type = parameter.getType();
            } else if (element instanceof Field) {
                final Field field = ((Field) element);
                owner = field.getDeclaringClass();
                type = field.getType();
            } else {
                final Annotation annotation = element.getAnnotation(Nullable.class);
                throw new TypeException("Annotation @Nullable appled to wrong element "+ element, annotation);
            }
            if (type.isPrimitive()) {
                throw new TypeException("Annotation @Nullable applied to primitive " + type.getSimpleName(), owner);
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <em>true</em> if the specified <em>annotation type</em> is
     * a <em>marker annotation</em> (does not define any value, implied by
     * defaults or specified).
     */
    public static boolean isMarker(Class<? extends Annotation> annotationType) {
        notNull(annotationType, "Null annotation type");
        return annotationType.getDeclaredMethods().length == 0;
    }

    /**
     * Returns <em>true</em> if the specified {@link Annotation} is
     * a <em>marker annotation</em> (does not define any value, implied by
     * defaults or specified).
     */
    public static boolean isMarker(Annotation annotation) {
        notNull(annotation, "Null annotation");
        return isMarker(annotation.annotationType());
    }

    /**
     * Returns <em>true</em> if the specified <em>annotation type</em> is
     * annotated with the {@link Qualifier} annotation.
     */
    public static boolean isQualifier(Class<? extends Annotation> annotationType) {
        notNull(annotationType, "Null annotation type");
        return annotationType.isAnnotationPresent(Qualifier.class);
    }

    /**
     * Returns <em>true</em> if the specified {@link Annotation} is
     * annotated with the {@link Qualifier} annotation.
     */
    public static boolean isQualifier(Annotation annotation) {
        notNull(annotation, "Null annotation");
        return isQualifier(annotation.annotationType());
    }

    /**
     * Returns <em>true</em> if the specified <em>annotation type</em> is
     * {@linkplain RetentionPolicy#RUNTIME retained at run-time}.
     */
    public static boolean isRetained(Class<? extends Annotation> annotationType) {
        notNull(annotationType, "Null annotation type");
        final Retention retention = annotationType.getAnnotation(Retention.class);
        return ((retention != null) && (retention.value() == RetentionPolicy.RUNTIME));
    }

    /**
     * Returns <em>true</em> if the specified {@link Annotation} is
     * {@linkplain RetentionPolicy#RUNTIME retained at run-time}.
     */
    public static boolean isRetained(Annotation annotation) {
        notNull(annotation, "Null annotation");
        return isRetained(annotation.annotationType());
    }

    /**
     * Find and validate the {@link ProvidedBy} annotation value, or return
     * <b>null</b> if not annotated.
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<? extends Provider<T>> findProvidedBy(Class<T> type) {
        notNull(type, "Null class");
        final ProvidedBy providedBy = type.getAnnotation(ProvidedBy.class);
        if (providedBy == null) return null;
        final Class<? extends Provider<?>> providerClass = providedBy.value();
        final Class<?> providedClass = Types.getTypeArguments(Provider.class, providerClass).get(0);
        if (type.isAssignableFrom(providedClass))
            return TypeLiteral.of((Class<? extends Provider<T>>) providerClass);
        throw new TypeException("Invalid @ProvidedBy annotation", type, null, providedBy);
    }

    /**
     * Return the {@link Qualifier} annotation (if any) specified in the
     * given {@link AnnotatedElement}.
     */
    public static Annotation findQualifier(AnnotatedElement element) {
        notNull(element, "Null annotated element");

        final List<Annotation> annotations = new ArrayList<>();
        for (Annotation annotation: element.getAnnotations())
            if (isQualifier(annotation)) annotations.add(annotation);

        if (annotations.size() == 0) return null;
        if (annotations.size() == 1) return annotations.get(0);

        final Annotation[] array = annotations.toArray(new Annotation[annotations.size()]);

        if (element instanceof Parameter) {
            final Parameter parameter = (Parameter) element;
            final Member owner = parameter.getDeclaringExecutable();
            throw new TypeException("Multiple @Qualifier annotation detected on parameter " + parameter.getName(), owner, array);
        } else if (element instanceof Member) {
            throw new TypeException("Multiple @Qualifier annotation detected", ((Member) element), array);
        } else {
            throw new TypeException("Multiple @Qualifier annotation detected on "+ element, array);
        }
    }
}
