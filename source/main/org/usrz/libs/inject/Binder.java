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

import java.util.function.Consumer;

import javax.inject.Named;

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.inject.bind.Binding;
import org.usrz.libs.inject.bind.Bindings;

/**
 * A {@link Binder} records bindings for injectables.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public interface Binder {

    /**
     * Bind the specified {@link Key} and return a {@link Linker} for it.
     */
    default <T> Linker<T> bind(Key<T> key) {
        notNull(key, "Null key");
        final Annotator<T> annotator = bind(key.getTypeLiteral());
        return key.getAnnotation() != null ? annotator.with(key.getAnnotation()) :
               key.getAnnotationType() != null ? annotator.with(key.getAnnotationType()) :
               annotator;
    }

    /**
     * Bind the specified {@link Class} and return a {@link Annotator} capable
     * of annotating and/or linking.
     */
    default <T> Annotator<T> bind(Class<T> type) {
        return bind(TypeLiteral.of(notNull(type, "Null class")));
    }

    /**
     * Bind the specified {@link TypeLiteral} and return a {@link Annotator}
     * capable of annotating and/or linking.
     */
    public <T> Annotator<T> bind(TypeLiteral<T> type);

    /* ====================================================================== */

    /**
     * Invoke the specified {@link Consumer} which will configure this
     * {@link Binder} instance.
     */
    default Binder install(Consumer<Binder> module) {
        notNull(module, "Null module").accept(this);
        return this;
    }

    /* ====================================================================== */

    /**
     * Create a <em>child</em> {@link Binder} to this instance.
     */
    public Binder isolate();

    /**
     * Invoke the specifed {@link Consumer} which will configure a new
     * {@linkplain #isolate() child} {@link Binder}.
     */
    default void isolate(Consumer<Binder> module) {
        final Binder binder = isolate();
        notNull(module, "Null module").accept(binder);
    }

    /* ====================================================================== */

    /**
     * Expose the binding associated with the specified {@link Key} to a
     * parent {@link Binder} (if any).
     */
    default void expose(Key<?> key) {
        notNull(key, "Null key");
        final Exposer annotator = expose(key.getTypeLiteral());
        if (key.getAnnotation() != null) annotator.with(key.getAnnotation());
        else if (key.getAnnotationType() != null) annotator.with(key.getAnnotationType());
    }

    /**
     * Expose the binding associated with the specified {@link Class} to a
     * parent {@link Binder} (if any).
     */
    default Exposer expose(Class<?> type) {
        return expose(TypeLiteral.of(notNull(type, "Null class")));
    }

    /**
     * Expose the binding associated with the specified {@link TypeLiteral}
     * to a parent {@link Binder} (if any).
     */
    public <T> Exposer expose(TypeLiteral<T> type);

    /* ====================================================================== */

    /**
     * Bind a {@link Configurations} instance which will be used to provide
     * {@link Named}-annotated constants.
     */
    default void configure(Configurations configurations) {
        this.bind(Configurations.class).toInstance(notNull(configurations, "Null configurations"));
    }

    /* ====================================================================== */

    /**
     * Return the {@link Bindings} associated with this {@link Binder} in the
     * current state.
     */
    public Bindings getBindings();

    /**
     * Inform this {@link Binder} that the specified {@link Class}es are to
     * be ignored when attempting to determine a
     * {@linkplain Binding#getSource() binding's source}.
     */
    public void ignoreSources(Class<?>... sources);

}
