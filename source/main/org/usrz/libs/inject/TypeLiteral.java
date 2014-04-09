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

import static org.usrz.libs.inject.utils.Parameters.notNull;
import static org.usrz.libs.inject.utils.Types.getClassForType;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.usrz.libs.inject.utils.Types;

/**
 * An abstraction of a generic type, as Java is not very good at describing
 * those...
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public abstract class TypeLiteral<T> {

    private static final TypeLiteral<Object> OBJECT = new TypeLiteralImpl<Object>(Object.class, Object.class, Collections.emptyList());

    private final Type type;
    private final Class<T> rawType;
    private final List<TypeLiteral<?>> parameters;

    private TypeLiteral(Type type, Class<T> rawType, List<TypeLiteral<?>> parameters) {
        this.type = notNull(type, "Null generic type");
        this.rawType = notNull(rawType, "Null raw type");
        this.parameters = notNull(parameters, "Null parameters");
    }

    /**
     * Create a new {@link TypeLiteral} instance.
     * <p>
     * As with all other JSR-330 implementations, this will look somehow
     * similar to {@code new TypeLiteral<Map<String,Integer>>()}
     */
    @SuppressWarnings("unchecked")
    protected TypeLiteral()
    throws TypeException {

        /* Get the generic superclass */
        final Type generic = getClass().getGenericSuperclass();
        if (!(generic instanceof ParameterizedType))
            throw new TypeException("Type is not a ParameterizedType", generic);

        /* Access type parameters */
        final Type[] parameters = ((ParameterizedType) generic).getActualTypeArguments();
        if (parameters.length != 1)
            throw new TypeException("Type must have only one parameter", generic);

        /* Construct this instance from the first parameter */
        final TypeLiteral<T> literal = (TypeLiteral<T>) of(parameters[0]);
        this.type = literal.getType();
        this.rawType = literal.getRawClass();
        this.parameters = literal.getParameters();
    }

    /**
     * Return the {@link Type} associated with this.
     */
    public final Type getType() {
        return type;
    }

    /**
     * Return the base raw {@link Class} associated with this.
     */
    public final Class<T> getRawClass() {
        return rawType;
    }

    /**
     * Return a non-<b>null</b> (but possibly empty) of all {@link TypeLiteral}
     * parameters associated with this.
     */
    public final List<TypeLiteral<?>> getParameters() {
        return this.parameters;
    }

    /* ====================================================================== */

    final StringBuilder toString(StringBuilder builder) {
        builder.append((rawType.isArray() ? rawType.getComponentType() : rawType).getSimpleName());

        if (parameters.size() > 0) {
            char separator = '<';
            for (TypeLiteral<?> parameter: parameters) {
                builder.append(separator);
                parameter.toString(builder);
                separator = ',';
            }
            builder.append('>');
        }
        if (rawType.isArray()) builder.append("[]");

        return builder;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder("TypeLiteral["))
                .append(']')
                .append('@')
                .append(Integer.toHexString(hashCode()))
                .toString();
    }

    /* ====================================================================== */

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        try {
            final TypeLiteral<?> literal = (TypeLiteral<?>) object;
            if (!this.rawType.equals(literal.rawType)) return false;
            if (this.parameters.size() != literal.parameters.size()) return false;
            for (int x = 0; x < parameters.size(); x ++) {
                if (this.parameters.get(x).equals(literal.parameters.get(x))) continue;
                return false;
            }
            return true;
        } catch (ClassCastException exception) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.rawType.hashCode() ^ this.parameters.hashCode();
    }

    /* ====================================================================== */

    /**
     * Return a {@link TypeLiteral} from an object instance.
     */
    @SuppressWarnings("unchecked")
    public static final <T> TypeLiteral<T> from(T instance) {
        return (TypeLiteral<T>) of(instance.getClass());
    }

    /**
     * Return a {@link TypeLiteral} from a {@link Type}.
     */
    public static final TypeLiteral<?> of(Type type)
    throws TypeException {
        notNull(type, "Null type");
        return OBJECT.resolve(type);
    }

    /**
     * Return a {@link TypeLiteral} from a {@link Class}.
     */
    public static final <T> TypeLiteral<T> of(Class<T> type) {
        /* NOTE: Can we use instance.getClass().getTypeParameters() ??? */
        notNull(type, "Null type");

        /* Remeember to use "getClassForType" to normalise primitives */
        @SuppressWarnings("unchecked")
        final Class<T> clazz = (Class<T>) getClassForType(type);
        return new TypeLiteralImpl<T>(type, clazz, Collections.emptyList());
    }

    /* ====================================================================== */

    /**
     * Resolve the specified {@link Type} against this.
     * <p>
     * This method will take into consideration the type variables declared
     * by this instance, and (purposedly) resolve them. This is used to figure
     * out what actual {@link Type}s to inject in dependencies.
     */
    public TypeLiteral<?> resolve(Type type) {

        /* Simple case, normal class */
        if (type instanceof Class)
            return new TypeLiteralImpl<>(type, getClassForType(type), Collections.emptyList());

        /* Parameterized type, needs some digging... */
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterized = (ParameterizedType) type;

            /* Resolve the TypeLiteral(s) for all parameters */
            final List<TypeLiteral<?>> parameters = new ArrayList<>();
            for (Type parameter: parameterized.getActualTypeArguments()) {
                parameters.add(resolve(parameter));
            }

            return new TypeLiteralImpl<>(type, getClassForType(type), parameters);
        }

        /* Generic array types are a bit nasty */
        if (type instanceof GenericArrayType) {
            final GenericArrayType array = (GenericArrayType) type;

            final TypeLiteral<?> component = resolve(array.getGenericComponentType());
            return new TypeLiteralImpl<>(type, getClassForType(type), component.getParameters());

        }

        /* Type variables are the worse! */
        if (type instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) type;
            Type resolved = Types.resolveVariableType(this.type, rawType, variable);
            if (resolved != variable) return resolve(resolved);
        }

        /* All other types will throw an exception */
        throw new TypeException("Unable to resolve type " + type, this.type);

    }

    /* ====================================================================== */

    private static final class TypeLiteralImpl<T> extends TypeLiteral<T> {

        private TypeLiteralImpl(Type type, Class<T> rawType, List<TypeLiteral<?>> parameters) {
            super(type, rawType, parameters);
        }
    }

}
