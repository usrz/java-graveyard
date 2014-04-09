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

import static org.usrz.libs.inject.utils.Annotations.isOptional;
import static org.usrz.libs.inject.utils.Parameters.notNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.TypeLiteral;

/**
 * A {@link MemberInjector} for {@link Method}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class MethodInjector<T>
extends ExecutableDescriptor<T, Method>
implements MemberInjector<T> {

    private final boolean optional;

    /**
     * Create a new {@link MethodInjector} associated with the specified
     * {@link TypeLiteral} and one of its {@link Method}s.
     */
    public MethodInjector(TypeLiteral<T> type, Method method) {
        super(type, method);
        optional = isOptional(method);
    }

    @Override
    public void inject(Injector injector, Object object)
    throws InjectionException {
        notNull(object, "Null object to inject");

        /* Get the method's arguments */
        final Object[] arguments = resolveArguments(injector);

        /*
         * If this method is marked @Optiona, then check if all parameters are
         * "null"s. If so we don't even invoke it at all...
         */
        if (optional) {
            boolean skipInvocation = true;
            for (Object argument: arguments) {
                if (argument == null) continue;
                skipInvocation = false;
                break;
            }
            if (skipInvocation) return;
        }

        /* Either we found a non-"null" argument, or this method was not marked
         * with the @Optional annotation. In both
         */
        try {
            executable.invoke(object, arguments);
        } catch (InvocationTargetException exception) {
            final Class<?> target = object instanceof Class ? (Class<?>) object : object.getClass();
            throw new InjectionException("Exception invoking method " + executable + " on " + target, exception.getCause());
        } catch (Exception exception) {
            final Class<?> target = object instanceof Class ? (Class<?>) object : object.getClass();
            throw new InjectionException("Unable to invoke method " + executable + " on " + target, exception);
        }
    }
}
