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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.usrz.libs.inject.TypeException;

/**
 * Utilities for Java types.
 * <p>
 * These methods have been lifted quasi-as-is from Google Guice.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class Types {

    private Types() {
        throw new IllegalStateException("Do not construct");
    }

    private static int indexOf(Object[] array, Object instance) {
        for (int x = 0; x < array.length; x++) {
            if (instance.equals(array[x])) {
                return x;
            }
        }
        return -1;
    }

    /**
     * Return the {@link Class} of the specified {@link Type}.
     */
    public static Class<?> getClassForType(Type type)
    throws TypeException {

        /* Simple, but convert primitives */
        if (type instanceof Class) {
            final Class<?> clazz = (Class<?>) type;
            if (! clazz.isPrimitive()) return clazz;

            if (clazz == boolean.class) return Boolean.class;
            if (clazz == byte.class)    return Byte.class;
            if (clazz == char.class)    return Character.class;
            if (clazz == short.class)   return Short.class;
            if (clazz == int.class)     return Integer.class;
            if (clazz == long.class)    return Long.class;
            if (clazz == float.class)   return Float.class;
            if (clazz == double.class)  return Double.class;
            throw new TypeException("Unsupported primitive", clazz);
        }

        /* Parameterized type */
        if (type instanceof ParameterizedType)
            return getClassForType(((ParameterizedType) type).getRawType());

        /* Generic array type */
        if (type instanceof GenericArrayType) {
            final Type componentType = ((GenericArrayType) type).getGenericComponentType();
            final Class<?> componentClass = getClassForType(componentType);
            if (componentClass != null)
                return Array.newInstance(componentClass, 0).getClass();
        }

        /* Variable type */
        throw new TypeException("Unable to resolve base class", type);
    }

    /**
     * Resolve the specified {@link TypeVariable} against the given
     * {@link Type}.
     */
    public static Type resolveVariableType(Type type, Class<?> rawType, TypeVariable<?> variable) {
        final GenericDeclaration declaration = variable.getGenericDeclaration();
        final Class<?> declaringClass = declaration instanceof Class ? (Class<?>) declaration : null;

        /* The variable was NOT declared on a class */
        if (declaringClass == null) {
            throw new TypeException("Unable to resolve ype variable " + variable + " declared on " + declaration, ((Member) declaration).getDeclaringClass());
        }

        /* Let's figure out the parameters */
        final Type declaredBy = getGenericSupertype(type, rawType, declaringClass);
        if (declaredBy instanceof ParameterizedType) {
            int index = indexOf(declaringClass.getTypeParameters(), variable);
            return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
        }

        /* Nada */
        throw new TypeException("Unable to resolve type variable", variable);
    }

    /**
     * Return the <em>generic supertype</em> of the given type.
     * {@link Type}.
     */
    public static Type getGenericSupertype(Type type, Class<?> rawType, Class<?> toResolve) {
        /* Same class/class to resolve? Done */
        if (toResolve == rawType) return type;

        /* Check interfaces if this is one */
        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (interfaces[i] == toResolve) {
                    return rawType.getGenericInterfaces()[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                }
            }
        }

        /* Check super classes if not an interface */
        if (!rawType.isInterface()) {
            while (rawType != Object.class) {
                final Class<?> rawSupertype = rawType.getSuperclass();
                if (rawSupertype == toResolve) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(rawSupertype)) {
                    return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                }
                rawType = rawSupertype;
            }
        }

        /* Can't go on */
        return toResolve;
    }

    /**
     * Get the actual type arguments a child class has used to extend a generic
     * base class.
     *
     * <p>Normally this would be invoked as:</p>
     *
     * <pre>getTypeArguments(AbstractTypeClass.class, this.getClass());</pre>
     *
     * @param baseClass the base {@link Class}
     * @param childClass the child {@link Class}
     * @return A {@link List} of the raw classes for the actual type arguments.
     * @see <a href="http://www.artima.com/weblogs/viewpost.jsp?thread=208860">Ian
     *      Robertson - Reflecting generics (June 23, 2007)</a>
     */
    public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass) {

        /* Descend and discover, deep inside the child class super classes and interfaces */
        final Map<TypeVariable<?>, Type> resolvedTypes = new HashMap<TypeVariable<?>, Type>();
        resolveTypeArguments(childClass, resolvedTypes);

        /* Resolve types by chasing down type variables. */
        final List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
        for (Type baseType: baseClass.getTypeParameters()) {
            while (resolvedTypes.containsKey(baseType)) {
                baseType = resolvedTypes.get(baseType);
            }
            typeArgumentsAsClasses.add(getClassForType(baseType));
        }

        /* All done */
        return typeArgumentsAsClasses;
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
