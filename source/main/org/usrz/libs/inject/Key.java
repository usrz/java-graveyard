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
package org.usrz.libs.inject;

import static org.usrz.libs.inject.utils.Parameters.notNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Provider;

import org.usrz.libs.inject.utils.Annotations;

/**
 * A {@link Key} identifies an object participating in injection.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <T> The type of this {@link Key}
 */
public final class Key<T> {

    private final TypeLiteral<T> literal;
    private final Class<? extends Annotation> annotationType;
    private final Annotation annotation;

    private Key(TypeLiteral<T> literal, Class<? extends Annotation> annotationType, Annotation annotation) {
        this.literal = notNull(literal, "Null type");
        this.annotationType = annotationType;
        this.annotation = annotation;
    }

    /* ====================================================================== */

    /**
     * Return the {@link TypeLiteral} associated with this {@link Key}.
     */
    public final TypeLiteral<T> getTypeLiteral() {
        return literal;
    }

    /**
     * Return the <em>annotation type</em> associated with this {@link Key}
     * or <b>null</b>.
     */
    public final Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }

    /**
     * Return the {@link Annotation} associated with this {@link Key}
     * or <b>null</b>.
     * <p>
     * If this method returns a non-<b>null</b> {@link Annotation}, the
     * {@link #getAnnotationType()} method will return the
     * {@linkplain Annotation#annotationType() annotation type} of the
     * returned instance.
     */
    public final Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Check if this key is associated with a provider, or if in other words
     * it is a {@code Key<Provider<?>>}.
     */
    public boolean isProviderKey() {
        return Provider.class.equals(literal.getRawClass());
    }

    /**
     * Returns <b>true</b> if the specified type is the same as what
     * is represented by this {@link Key}.
     */
    public boolean isTypeOf(Class<?> type) {
        return literal.getRawClass().equals(type);
    }

    /**
     * Cast the specified {@link Object} instance to the type specified by
     * this {@link Key}.
     *
     * @return The casted instance or <b>null</b> if that was specified.
     * @throws ClassCastException If the instance can not be casted.
     */
    public T cast(Object instance) {
        if (instance == null) return null;
        return this.literal.getRawClass().cast(instance);
    }

    /* ====================================================================== */

    /**
     * Create a new {@link Key} from this overriding its {@link Annotation}.
     */
    public <X> Key<X> with(TypeLiteral<X> type) {
        return new Key<X>(notNull(type, "Null type"), annotationType, annotation);
    }

    /**
     * Create a new {@link Key} from this overriding its {@link Annotation}.
     */
    public Key<T> with(Annotation annotation) {
        return Key.of(this.literal, annotation);
    }

    /**
     * Create a new {@link Key} from this overriding its <em>annotation
     * type</em>.
     */
    public Key<T> with(Class<? extends Annotation> annotationType) {
        return Key.of(this.literal, annotationType);
    }

    /* ====================================================================== */

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("Key[");
        this.literal.toString(builder);
        if (this.annotation != null) {
            builder.append(",annotation=").append(annotation.toString());
        } else if (this.annotationType != null) {
            builder.append(",annotationType=").append(annotationType.toString());
        }
        return builder.append(']')
                      .append('@')
                      .append(Integer.toHexString(hashCode()))
                      .toString();
    }

    @Override
    public int hashCode() {
        int hash = literal.hashCode();
        if (annotationType != null) {
            hash = (31 * hash) + annotationType.hashCode();
            if (annotation != null) {
                hash = (31 * hash) + annotation.hashCode();
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        try {
            final Key<?> key = (Key<?>) object;
            if (!this.literal.equals(key.literal)) return false;
            return this.annotationType == null ? key.annotationType == null :
                            this.annotationType.equals(key.annotationType)
                && this.annotation == null ? key.annotation == null :
                      this.annotation.equals(key.annotation);
        } catch (ClassCastException exception) {
            return false;
        }
    }

    /* ====================================================================== */

    /**
     * Return a {@link Key} from the specified {@link Type}.
     */
    public static Key<?> of(Type type) {
        return of(TypeLiteral.of(notNull(type, "Null type")), null, null);
    }

    /**
     * Return a {@link Key} from the specified {@link Type}
     * and {@link Annotation}.
     */
    public static Key<?> of(Type type, Annotation annotation) {
        return of(TypeLiteral.of(notNull(type, "Null type")), null, annotation);
    }

    /**
     * Return a {@link Key} from the specified {@link Type}
     * and <em>annotation type</em>.
     */
    public static Key<?> of(Type type, Class<? extends Annotation> annotationType) {
        return of(TypeLiteral.of(notNull(type, "Null type")), annotationType, null);
    }

    /**
     * Return a {@link Key} from the specified {@link Class}.
     */
    public static <T> Key<T> of(Class<T> type) {
        return of(TypeLiteral.of(notNull(type, "Null class")), null, null);
    }

    /**
     * Return a {@link Key} from the specified {@link Class}
     * and {@link Annotation}.
     */
    public static <T> Key<T> of(Class<T> type, Annotation annotation) {
        return of(TypeLiteral.of(notNull(type, "Null class")), null, annotation);
    }

    /**
     * Return a {@link Key} from the specified {@link Class}
     * and <em>annotation type</em>.
     */
    public static <T> Key<T> of(Class<T> type, Class<? extends Annotation> annotationType) {
        return of(TypeLiteral.of(notNull(type, "Null class")), annotationType, null);
    }

    /**
     * Return a {@link Key} from the specified {@link TypeLiteral}.
     */
    public static <T> Key<T> of(TypeLiteral<T> type) {
        return of(notNull(type, "Null type literal"), null, null);
    }

    /**
     * Return a {@link Key} from the specified {@link TypeLiteral}
     * and {@link Annotation}.
     */
    public static <T> Key<T> of(TypeLiteral<T> type, Annotation annotation) {
        return of(notNull(type, "Null type literal"), null, annotation);
    }

    /**
     * Return a {@link Key} from the specified {@link TypeLiteral}
     * and <em>annotation type</em>.
     */
    public static <T> Key<T> of(TypeLiteral<T> type, Class<? extends Annotation> annotationType) {
        return of(notNull(type, "Null type literal"), annotationType, null);
    }

    /* ====================================================================== */

    private static <T> Key<T> of(TypeLiteral<T> literal, Class<? extends Annotation> annotationType, Annotation annotation) {

        /* Normalize annotation and annotation types */
        if ((annotation != null) && (annotationType == null)) annotationType = annotation.annotationType();
        if (annotationType != null) {

            /* Is this a qualifier annotation? */
            if (! Annotations.isQualifier(annotationType))
                throw new TypeException("Annotation is not annotated with @Qualifier", annotationType);

            /* Is this retained at runtime? */
            if (! Annotations.isRetained(annotationType))
                throw new TypeException("Annotation is not annotated with @RetentionPolicy(RUNTIME)", annotationType);

            /* Is this a "marker" annotation or should it be fully specified */
            if (Annotations.isMarker(annotationType)) {
                annotation = null;
            } else if (annotation == null) {
                throw new TypeException("Non-marker annotation must be fully specified (needs an instance)", annotationType);
            }
        }

        return new Key<T>(literal, annotationType, annotation);
    }
}
