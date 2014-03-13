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
package org.usrz.libs.utils.converters;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class to find the types of a parameterized object.
 *
 * @see <A href="http://www.artima.com/weblogs/viewpost.jsp?thread=208860">Ian
 *      Robertson - Reflecting generics (June 23, 2007)</A>
 */
public class TypeFinder {

    private TypeFinder() {
        throw new IllegalStateException("Go away!");
    }

    /**
     * Get the underlying @{link Class} for a {@link Type}, or <b>null</b> if
     * the {@link Type} is a variable type.

     * @param type the {@link Type}
     * @return the underlying {@link Class} or <b>null</b>
     */
    public static Class<?> getClass(Type type) {

        /* Simple */
        if (type instanceof Class)
            return (Class<?>) type;

        /* Parameterized type */
        if (type instanceof ParameterizedType)
            return getClass(((ParameterizedType) type).getRawType());

        /* Generic array type */
        if (type instanceof GenericArrayType) {
            final Type componentType = ((GenericArrayType) type).getGenericComponentType();
            final Class<?> componentClass = getClass(componentType);
            if (componentClass != null)
                return Array.newInstance(componentClass, 0).getClass();
        }

        /* Variable type */
        return null;
    }

    /**
     * Get the actual type arguments a child class has used to extend a generic
     * base class.
     *
     * <P>Normally this would be invoked as:</P>
     *
     * <PRE>getTypeArguments(AbstractTypeClass.class, this.getClass());</PRE>
     *
     * @param baseClass the base {@link Class}
     * @param childClass the child {@link Class}
     * @return A {@link List} of the raw classes for the actual type arguments.
     */
    public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass) {

        /* Descend and discover, deep inside the child class super classes and interfaces */
        final Map<TypeVariable<?>, Type> resolvedTypes = resolveTypeArguments(childClass);

        /* Resolve types by chasing down type variables. */
        final List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
        for (Type baseType: baseClass.getTypeParameters()) {
            while (resolvedTypes.containsKey(baseType)) {
                baseType = resolvedTypes.get(baseType);
            }
            typeArgumentsAsClasses.add(getClass(baseType));
        }

        /* All done */
        return typeArgumentsAsClasses;
    }

    /**
     * Walk the entire hierarchy of a class (all super classes and implemented
     * interfaces) and resolve all possible type variables.
     */
    public static Map<TypeVariable<?>, Type> resolveTypeArguments(Class<?> classType) {
        final Map<TypeVariable<?>, Type> resolvedTypes = new HashMap<TypeVariable<?>, Type>();
        resolveTypeArguments(classType, resolvedTypes);
        return resolvedTypes;
    }

    /* Recursively descend into the type's hierarchy */
    private static void resolveTypeArguments(Type type, Map<TypeVariable<?>, Type> resolvedTypes) {
        if (type == null) return;

        if (type instanceof Class) {
            /*
             * There is no useful information for us in raw types, so just
             * keep going
             */
            final Class<?> classType = ((Class<?>) type);
            resolveTypeArguments(classType.getGenericSuperclass(), resolvedTypes);
            for(Type interfaceType: classType.getGenericInterfaces()) {
                resolveTypeArguments(interfaceType, resolvedTypes);
            }

        } else {

            final ParameterizedType parameterizedType = (ParameterizedType) type;
            final Class<?> rawType = (Class<?>) parameterizedType.getRawType();

            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            final TypeVariable<?>[] typeParameters = rawType.getTypeParameters();

            for (int i = 0; i < actualTypeArguments.length; i++) {
                resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
            }

            resolveTypeArguments(rawType.getGenericSuperclass(), resolvedTypes);
            for(Type interfaceType: rawType.getGenericInterfaces()) {
                resolveTypeArguments(interfaceType, resolvedTypes);
            }
        }
    }

}
