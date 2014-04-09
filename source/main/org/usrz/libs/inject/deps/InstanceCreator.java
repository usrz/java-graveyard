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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.TypeLiteral;
import org.usrz.libs.inject.bind.Resolver;

/**
 * An {@link InstanceCreator} simply creates instances of objects using a
 * {@link Constructor} and resolving its dependencies.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class InstanceCreator<T>
extends ExecutableDescriptor<T, Constructor<? extends T>>
implements Resolver<T> {

    /**
     * Create a new {@link InstanceCreator} associated with the specified
     * {@link TypeLiteral} and one of its {@link Constructor}s.
     */
    public InstanceCreator(TypeLiteral<T> type, Constructor<? extends T> constructor) {
        super(type, constructor);
    }

    /**
     * Create a new instance of the object, resolving the {@link Constructor}'s
     * dependencies using the specified {@link Injector}.
     */
    @Override
    public T resolve(Injector injector)
    throws InjectionException {
        final Object[] arguments = resolveArguments(injector);
        try {
            return executable.newInstance(arguments);
        } catch (InvocationTargetException exception) {
            final Class<?> target = executable.getDeclaringClass();
            throw new InjectionException("Exception invoking constructor on " + target, exception.getCause());
        } catch (Exception exception) {
            final Class<?> target = executable.getDeclaringClass();
            throw new InjectionException("Unable to create instance of " + target, exception);
        }
    }

}
