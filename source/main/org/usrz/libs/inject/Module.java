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

import java.util.function.Consumer;

import javax.inject.Named;

import org.usrz.libs.configurations.Configurations;

/**
 * A {@link Module} is a non-{@linkplain FunctionalInterface functional}
 * way to configure a {@link Binder} with its bindings.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public abstract class Module implements Consumer<Binder> {

    private final ThreadLocal<Binder> binder = new ThreadLocal<>();

    /**
     * Create a new {@link Module} instance.
     */
    protected Module() {
        /* Nothing to do */
    }

    /**
     * Start configuring the {@link Binder}.
     */
    @Override
    public void accept(Binder binder) {
        if (this.binder.get() != null) {
            throw new IllegalStateException("Binder already specified in current thread");
        } else try {
            configure();
        } finally {
            this.binder.remove();
        }
    }

    /**
     * Method to be implemented to configure the bindings.
     */
    protected abstract void configure();

    /* ====================================================================== */

    protected final Binder binder() {
        final Binder binder = this.binder.get();
        if (binder == null) throw new IllegalStateException("No binder available");
        return binder;
    }

    /**
     * Bind the specified {@link Key} and return a {@link Linker} for it.
     */
    protected final <T> Linker<T> bind(Key<T> key) {
        return binder().bind(key);
    }

    /**
     * Bind the specified {@link Class} and return a {@link Annotator} capable
     * of annotating and/or linking.
     */
    protected final <T> Annotator<T> bind(Class<T> type) {
        return binder().bind(type);
    }

    /**
     * Bind the specified {@link TypeLiteral} and return a {@link Annotator}
     * capable of annotating and/or linking.
     */
    protected final <T> Annotator<T> bind(TypeLiteral<T> type) {
        return binder().bind(type);
    }

    /**
     * Invoke the specified {@link Consumer} which will configure this
     * {@link Binder} instance.
     */
    protected final Binder install(Consumer<Binder> module) {
        return binder().install(module);
    }

    /**
     * Create a <em>child</em> {@link Binder} to this instance.
     */
    protected final Binder isolate() {
        return binder().isolate();
    }

    /**
     * Expose the binding associated with the specified {@link Key} to a
     * parent {@link Binder} (if any).
     */
    protected final void expose(Key<?> key)  {
        binder().expose(key);
    }

    /**
     * Expose the binding associated with the specified {@link Class} to a
     * parent {@link Binder} (if any).
     */
    protected final Exposer expose(Class<?> type) {
        return binder().expose(type);
    }

    /**
     * Expose the binding associated with the specified {@link TypeLiteral}
     * to a parent {@link Binder} (if any).
     */
    protected final <T> Exposer expose(TypeLiteral<T> type) {
        return binder().expose(type);
    }

    /**
     * Bind a {@link Configurations} instance which will be used to provide
     * {@link Named}-annotated constants.
     */
    protected final void configure(Configurations configurations) {
        binder().configure(configurations);
    }

}
