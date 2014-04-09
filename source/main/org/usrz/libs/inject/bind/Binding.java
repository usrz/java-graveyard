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
package org.usrz.libs.inject.bind;

import static org.usrz.libs.inject.utils.Parameters.notNull;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.Key;
import org.usrz.libs.inject.ProvisionException;

/**
 * The core association between a {@link Key} and a {@link Resolver} producing
 * instances for it.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <T> The type of the instances to produce.
 */
public final class Binding<T> implements Resolver<T> {

    private final Key<T> key;
    private final Resolver<? extends T> resolver;
    private final boolean eagerlyInjected;
    private final String source;

    /**
     * Create a new {@link Binding} instance.
     *
     * @param key The <b>non-null</b> {@link Key} for this {@link Binding}
     * @param resolver The <b>non-null</b> {@link Resolver} of this {@link Binding}
     */
    public Binding(Key<T> key, Resolver<? extends T> resolver) {
        this(key, resolver, false, null);
    }

    /**
     * Create a new {@link Binding} instance.
     *
     * @param key The <b>non-null</b> {@link Key} for this {@link Binding}.
     * @param resolver The <b>non-null</b> {@link Resolver} of this {@link Binding}.
     * @param eagerlyInjected Whether this {@link Binding} should be <em>eagerly
     *                        injected</em> by the {@link Injector} upon its
     *                        creation.
     * @param source An optional description specifying where this
     *               {@link Binding} was constructed.
     */
    public Binding(Key<T> key, Resolver<? extends T> resolver, boolean eagerlyInjected, String source) {
        this.key = notNull(key, "Null key (source " + source + ")");
        this.resolver = notNull(resolver, "Null resolver (key " + key + " source " + source + ")");
        this.eagerlyInjected = eagerlyInjected;
        this.source = source;
        if (key.isProviderKey()) {
            throw new ProvisionException("Providers must be bound to the type they produce", key, this);
        }
    }

    /**
     * Return the <b>non-null</b> {@link Key} associated with this
     * {@link Binding}.
     */
    public Key<T> getKey() {
        return key;
    }

    /**
     * Delegate object creation to the {@link Resolver} specified at
     * {@link #Binding(Key, Resolver, String)} construction.
     */
    @Override
    public T resolve(Injector injector)
    throws InjectionException {
        return resolver.resolve(injector);
    }

    /**
     * Return whether this {@link Binding} should be <em>eagerly injected</em>
     * by the {@link Injector} upon its creation.
     */
    public boolean isEagerlyInjected() {
        return eagerlyInjected;
    }

    /**
     * Return a {@link String} identifying where this {@link Binding} was
     * defined or <b>null</b>
     */
    public String getSource() {
        return source;
    }

}
