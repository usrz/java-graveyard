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

import java.util.Set;
import java.util.function.Consumer;

import org.usrz.libs.inject.bind.Bindings;
import org.usrz.libs.inject.impl.BinderImpl;
import org.usrz.libs.inject.impl.InjectorImpl;

/**
 * The root of all evil...
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public interface Injector {

    /**
     * Create an {@link Injector} only supporting <em>dynamic bindings</em>.
     * <p>
     * Note that the returned {@link Injector} <em>will not</em> support
     * static injection.
     */
    public static Injector create() {
        return create(false, (binder)->{});
    }

    /**
     * Create an {@link Injector} using the specified {@link Consumer} to
     * configure a {@link Binder}.
     * <p>
     * Note that the returned {@link Injector} <em>will not</em> support
     * static injection.
     */
    public static Injector create(Consumer<Binder> module) {
        return create(false, module);
    }

    /**
     * Create an {@link Injector} using the specified {@link Consumer} to
     * configure a {@link Binder} and optionally supporting static injections.
     */
    public static Injector create(boolean supportStaticInjections, Consumer<Binder> module) {
        final Bindings bindings = new BinderImpl().install(module).getBindings();
        return new InjectorImpl(supportStaticInjections, bindings);
    }

    /**
     * Create a child {@link Injector} of this instance using the specified
     * {@link Consumer} to configure its {@link Binder}.
     */
    default Injector createChild(Consumer<Binder> module) {
        final Binder binder = new BinderImpl();
        binder.install(module);
        return createChild(binder.getBindings());
    }

    /**
     * Create a child {@link Injector} of this instance configured from the
     * specified {@link Bindings}.
     */
    public Injector createChild(Bindings bindings);

    /* ====================================================================== */

    /**
     * Check if the specified {@link Key} is bound into this {@link Injector}
     * or any of its parents.
     */
    public boolean isBound(Key<?> key);

    /**
     * Return a {@link Set} of all bound {@link Key}s.
     *
     * @param includeDynamic Include <em>dynamic bindings</em> or not.
     * @param includeParent Include this {@link Injector}'s parent binding.
     */
    public Set<Key<?>> getBoundKeys(boolean includeDynamic, boolean includeParent);

    /**
     * Return a fully injected instance of the specified {@link Class}.
     */
    default <T> T getInstance(Class<T> type) {
        return getInstance(Key.of(type));
    }

    /**
     * Return a fully injected instance of the class identified by the
     * specified {@link TypeLiteral}.
     */
    default <T> T getInstance(TypeLiteral<T> type) {
        return getInstance(Key.of(type));
    }

    /**
     * Return a fully injected instance of the class identified by the
     * specified {@link Key}.
     */
    public <T> T getInstance(Key<T> key);

    /* ====================================================================== */

    /**
     * Inject the specified {@link Object} instance.
     */
    default <T> T injectMembers(T instance)
    throws InjectionException {
        return this.injectMembers(instance, TypeLiteral.from(instance));
    }

    /**
     * Inject the specified generic {@link Object} instance, resolving types
     * according to the specified {@link TypeLiteral}.
     */
    public <T> T injectMembers(T instance, TypeLiteral<T> type)
    throws InjectionException;

}
