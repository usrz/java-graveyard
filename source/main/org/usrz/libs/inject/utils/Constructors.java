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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.usrz.libs.inject.TypeException;

/**
 * {@link Constructor}s utilities.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class Constructors {

    private Constructors() {
        throw new IllegalStateException("Do not construct");
    }

    /**
     * Find the {@link Constructor} annotated with the {@link Inject} inject
     * annotation or its <em>empty, public</em> constructor.
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getConstructor(Class<T> clazz) {

        /* Check that we can construct the class */
        if (clazz.isInterface())
            throw new TypeException("Specified class is an interface", clazz);
        if (clazz.isPrimitive())
            throw new TypeException("Specified class is a primitive", clazz);
        if (Modifier.isAbstract(clazz.getModifiers()))
            throw new TypeException("Specified class is abstract", clazz);
        if ((clazz.getDeclaringClass() != null) && (! Modifier.isStatic(clazz.getModifiers())))
            throw new TypeException("Inner classes must be static", clazz);

        final List<Constructor<?>> constructors = new ArrayList<>();
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                constructors.add(constructor);
            }
        }

        if (constructors.size() == 1) {
            return (Constructor<T>) constructors.get(0);
        } else if (constructors.size() > 1) {
            throw new TypeException("Multiple constructors annotated with @Inject", clazz);
        } else try {
            return clazz.getConstructor();
        } catch (NoSuchMethodException exception) {
            throw new TypeException("No public empty or @Inject annotated constructor found", clazz);
        }
    }
}
