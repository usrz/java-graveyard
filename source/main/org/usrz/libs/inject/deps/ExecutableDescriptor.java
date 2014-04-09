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
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.Key;
import org.usrz.libs.inject.TypeException;
import org.usrz.libs.inject.TypeLiteral;
import org.usrz.libs.inject.bind.Resolver;
import org.usrz.libs.inject.utils.Annotations;

/**
 * A {@link Descriptor} for a {@link Method} or {@link Constructor}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public abstract class ExecutableDescriptor<T, E extends Executable>
extends AbstractDescriptor<T>
implements Descriptor<T> {

    /** The {@link Method} or {@link Constructor} associated with this. */
    protected final E executable;

    private final List<Dependency<?>> parameters;

    /**
     * Create a new {@link ExecutableDescriptor} given the specified
     * {@link TypeLiteral} and one of its {@link Method}s or
     * {@link Constructor}s
     */
    protected ExecutableDescriptor(TypeLiteral<T> type, E executable) {
        super(type);

        parameters = parameterDependencies(type, executable);
        this.executable = notNull(executable, "Null method/constructor");
        this.executable.setAccessible(true);
    }

    /**
     * Resolve the arguments needed to invoke the {@link Method} or
     * {@link Constructor}, using the specified {@link Injector}.
     *
     * @see Resolver#resolve(Injector)
     * @see MemberInjector#inject(Injector, Object)
     */
    protected Object[] resolveArguments(Injector injector)
    throws InjectionException {
        final Object[] arguments = new Object[parameters.size()];
        for (int x = 0; x < arguments.length; x ++) {
            arguments[x] = parameters.get(x).resolve(injector);
        }
        return arguments;
    }

    /* ====================================================================== */

    @Override
    public List<Dependency<?>> getDependencies() {
        return parameters;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + executable.toString() + "]";
    }

    /* ====================================================================== */

    private static List<Dependency<?>> parameterDependencies(TypeLiteral<?> type, Executable executable) {
        final Parameter[] parameters = executable.getParameters();
        final List<Dependency<?>> dependencies = new ArrayList<>(parameters.length);

        final Annotation executableAnnotation = Annotations.findQualifier(executable);
        if ((executableAnnotation != null) && (parameters.length != 1)) {
            throw new TypeException("@Qualifier annotation can be specified only for parameters OR methods/constructors with only one parameter", executable, executableAnnotation);
        }

        /*
         * The @Optional annotation applies to methods. Parameters simply
         * "inherit" this flag from the method they're defined in.
         */
        final boolean optionalCall = Annotations.isOptional(executable);

        for (Parameter parameter: parameters) {
            final Annotation parameterAnnotation = Annotations.findQualifier(parameter);
            if ((executableAnnotation != null) && (parameterAnnotation != null))
                throw new TypeException("@Qualifier parameter annotation specified both on method/constructor and parameter", executable, executableAnnotation, parameterAnnotation);

            final boolean nullable = Annotations.isNullable(parameter);
            final boolean optional = optionalCall || Annotations.isOptional(parameter);

            Key<?> key = Key.of(type.resolve(parameter.getParameterizedType()));
            if (parameterAnnotation != null) key = key.with(parameterAnnotation);
            if (executableAnnotation != null) key = key.with(executableAnnotation);
            dependencies.add(new Dependency<>(key, optional, nullable));
        }

        return Collections.unmodifiableList(dependencies);
    }

}
