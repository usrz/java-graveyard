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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConvertibleType {

    private final Class<?> rawType;
    private final ConvertibleType[] arguments;

    public ConvertibleType(Type type) {
        if (type == null) throw new NullPointerException("Null type");

        if (type instanceof Class<?>) {
            final Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) {
                rawType = Array.class;
                arguments = new ConvertibleType[1];
                arguments[0] = new ConvertibleType(clazz.getComponentType());
                return;
            }

            else {
                rawType = clazz;
                arguments = new ConvertibleType[0];
            }
        }

        else if (type instanceof GenericArrayType) {
            final GenericArrayType array = (GenericArrayType) type;
            rawType = Array.class;
            arguments = new ConvertibleType[1];
            arguments[0] = new ConvertibleType(array.getGenericComponentType());
            return;
        }

        else if (type instanceof ParameterizedType) {
            final ParameterizedType parameterized = (ParameterizedType) type;
            rawType = TypeFinder.getClass(parameterized.getRawType());
            if (rawType == null) throw new IllegalStateException("Unable to find raw type for " + type);

            final Type[] typeArguments = parameterized.getActualTypeArguments();
            arguments = new ConvertibleType[typeArguments.length];
            for (int x = 0; x < typeArguments.length; x ++) {
                arguments[x] = new ConvertibleType(typeArguments[x]);
            }

        }

        /* All else, fail!!! */
        else {
            throw new IllegalStateException("Unable to construct type for " + type);
        }
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public List<ConvertibleType> getArguments() {
        return Collections.unmodifiableList(Arrays.asList(arguments));
    }

    public boolean isAssignableFrom(ConvertibleType type) {
        if (rawType.isAssignableFrom(type.rawType)) {
            if (arguments.length == type.arguments.length) {
                for (int x = 0; x < arguments.length; x ++) {
                    if (arguments[x].isAssignableFrom(type.arguments[x])) {
                        continue;
                    } else {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(rawType.getName());
        if (arguments.length != 0) {
            char c = '<';
            for (ConvertibleType argument: arguments) {
                builder.append(c).append(argument);
                c = ',';
            }
            builder.append('>');
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        int hashCode = rawType.hashCode();
        for (ConvertibleType argument: arguments) hashCode ^= argument.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        try {
            /* Cast, if we can't return false */
            final ConvertibleType type = (ConvertibleType) object;

            /* Check the raw type */
            if (! rawType.equals(type.rawType)) return false;

            /* Check the number of arguments */
            if (arguments.length != type.arguments.length) return false;

            /* Check each argument */
            for (int x = 0; x < arguments.length; x ++) {
                if (arguments[x].equals(type.arguments[x])) continue;
                return false;
            }

            /* All arguments checked out */
            return true;

        } catch (ClassCastException exception) {
            return false;
        }
    }
}
