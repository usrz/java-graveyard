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
package org.usrz.libs.inject.deps;

import static org.usrz.libs.inject.utils.Parameters.notNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.Key;
import org.usrz.libs.inject.TypeException;
import org.usrz.libs.inject.TypeLiteral;
import org.usrz.libs.inject.utils.Annotations;

/**
 * A {@link FieldInjector} is a {@link Descriptor} and {@link MemberInjector}
 * for {@link Field}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class FieldInjector<T>
extends AbstractDescriptor<T>
implements MemberInjector<T> {

    private final Dependency<?> dependency;
    private final Field field;

    /**
     * Create a new {@link FieldInjector} associated with the specified
     * {@link TypeLiteral} and one of its {@link Field}s.
     */
    public FieldInjector(TypeLiteral<T> type, Field field) {
        super(type);

        this.field = notNull(field, "Null field");
        this.field.setAccessible(true);

        if (Modifier.isFinal(field.getModifiers())) {
            throw new TypeException("Field " + field + " can not be injected", type.getRawClass());
        }

        final boolean optional = Annotations.isOptional(field);
        final boolean nullable = Annotations.isNullable(field);

        Key<?> key = Key.of(type.resolve(field.getGenericType()));
        final Annotation annotation = Annotations.findQualifier(field);
        if (annotation != null) key = key.with(annotation);
        dependency = new Dependency<>(key, optional, nullable);
    }

    @Override
    public void inject(Injector injector, Object object)
    throws InjectionException {
        notNull(object, "Null instance");

        /*
         * If the field is marked as @Optional, then we don't invoke the
         * "set(instance, value) method, preserving any default/existing value,
         * while if it's "nullable", we forcedly set it to "null".
         */
        final Object value = dependency.resolve(injector);
        if ((value == null) && (dependency.isOptional())) return;

        try {
            field.set(object, value);
        } catch (Exception exception) {
            final Class<?> target = object instanceof Class ? (Class<?>) object : object.getClass();
            throw new InjectionException("Unable to set field " + field + " on " + target, exception);
        }
    }

    @Override
    public List<Dependency<?>> getDependencies() {
        return Collections.singletonList(dependency);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + field.toString() + "]";
    }

}
