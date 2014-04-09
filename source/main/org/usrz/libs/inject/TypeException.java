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

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

/**
 * An exception thrown whenever an error is encountered in a {@link Class},
 * its members or its annotation structure.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class TypeException extends IllegalStateException {

    public TypeException(String message, Type type) {
        super(prepareMessage(message, type, null));
    }

    public TypeException(String message, Annotation... annotations) {
        super(prepareMessage(message, null, null, annotations));
    }

    public TypeException(String message, Type type, Annotation... annotations) {
        super(prepareMessage(message, type, null, annotations));
    }

    public TypeException(String message, Member member, Annotation... annotations) {
        super(prepareMessage(message, member.getDeclaringClass(), member, annotations));
    }

    public static String prepareMessage(String message,
                                        Type type,
                                        Member member,
                                        Annotation... annotations) {
        final StringBuilder builder = new StringBuilder(message);

        if (type != null) builder.append("\n  Type: ").append(type);
        if (member != null) builder.append("\n  Member: ").append(member);

        String prefix = "\n  Annotation: ";
        if (annotations != null) for (Annotation annotation: annotations) {
            if (annotation == null) continue;
            builder.append(prefix).append(annotation);
            prefix = "\n              ";
        }

        return builder.toString();
    }

}
