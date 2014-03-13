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
package org.usrz.libs.riak;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public enum IndexType {

    BINARY("_bin"), INTEGER("_int");

    private final String suffix;

    private IndexType(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public static IndexType typeFor(Type type) {
        if (type == null) return null;

        if (type instanceof Class) {
            final Class<?> clazz = (Class<?>) type;

            if (clazz.isArray()) return typeFor(clazz.getComponentType());

            if ((byte.class.isAssignableFrom(clazz))    ||
                (short.class.isAssignableFrom(clazz))   ||
                (int.class.isAssignableFrom(clazz))     ||
                (long.class.isAssignableFrom(clazz))    ||
                (Byte.class.isAssignableFrom(clazz))    ||
                (Short.class.isAssignableFrom(clazz))   ||
                (Integer.class.isAssignableFrom(clazz)) ||
                (Long.class.isAssignableFrom(clazz)))
                    return IndexType.INTEGER;

            if (String.class.isAssignableFrom(clazz))
                return IndexType.BINARY;

        }

        if (type instanceof ParameterizedType) {
            // Basically just check "? extends Collection<Foo>"
            final ParameterizedType parameterized = (ParameterizedType) type;
            final Type rawType = parameterized.getRawType();
            if (rawType instanceof Class) {
                final Class<?> clazz = (Class<?>) rawType;
                if (Collection.class.isAssignableFrom(clazz)) {
                    final Type parameterType = parameterized.getActualTypeArguments()[0];
                    if (parameterType instanceof Class) {
                        return typeFor(parameterType);
                    }
                }
            }
        }

        /* Can not be mapped, apparently */
        return null;
    }
}
