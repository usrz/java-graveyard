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

import javax.inject.Provider;

import org.usrz.libs.inject.bind.Binding;

/**
 * An interface defining a way to specify the target of a {@link Binding}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <T> The type of binding this {@link Linker} refers to.
 */
public interface Linker<T> extends Initializer {

    /**
     * Inform a {@link Binder} that the bound dependency will be fulfilled
     * by the specified {@link Provider}.
     */
    public <X extends T, P extends Provider<X>> Initializer toProvider(P provider);

    /**
     * Inform a {@link Binder} that the bound dependency will be fulfilled
     * by the specified {@link Provider}.
     */
    default <X extends T, P extends Provider<X>> Initializer toProvider(Class<P> provider) {
        return toProvider(TypeLiteral.of(notNull(provider, "Null provider")));
    }

    /**
     * Inform a {@link Binder} that the bound dependency will be fulfilled
     * by the specified {@link Provider}.
     */
    public <X extends T, P extends Provider<X>> Initializer toProvider(TypeLiteral<P> selector);

    /**
     * Inform a {@link Binder} that the bound dependency will be fulfilled
     * by the specified {@link Class}.
     */
    default Initializer to(Class<? extends T> type) {
        return to(TypeLiteral.of(notNull(type, "Null class")));
    }

    /**
     * Inform a {@link Binder} that the bound dependency will be fulfilled
     * by the specified {@link TypeLiteral}.
     */
    public Initializer to(TypeLiteral<? extends T> type);

    /**
     * Inform a {@link Binder} that the bound dependency will be fulfilled
     * by the specified instance.
     */
    public Initializer toInstance(T instance);

}
