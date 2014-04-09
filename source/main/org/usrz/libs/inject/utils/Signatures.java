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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.usrz.libs.inject.TypeException;

/**
 * An utility class to calculate the standard JNI method signature.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class Signatures {

    private Signatures() {
        throw new IllegalStateException("Do not construct");
    }

    /**
     * Create the compact JNI-like signature for the specified method.
     */
    public static String signature(Method method) {
        final StringBuilder builder = new StringBuilder(method.getName()).append('(');
        for (Parameter parameter: method.getParameters())
            signature(builder, parameter.getType());
        return signature(builder.append(')'), method.getReturnType()).toString();
    }

    private static StringBuilder signature(StringBuilder builder, Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) return builder.append('Z');
            if (type == byte.class)    return builder.append('B');
            if (type == char.class)    return builder.append('C');
            if (type == double.class)  return builder.append('D');
            if (type == float.class)   return builder.append('F');
            if (type == int.class)     return builder.append('I');
            if (type == long.class)    return builder.append('J');
            if (type == short.class)   return builder.append('S');
            if (type == void.class)    return builder.append('V');
            throw new TypeException("Unknown primitive type", type);
        } else if (type.isArray()) {
            return signature(builder.append('['), type.getComponentType());
        } else {
            return builder.append('L').append(type.getName()).append(';');
        }
    }

}
