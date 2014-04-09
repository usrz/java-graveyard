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
package org.usrz.libs.inject.impl;

import static org.usrz.libs.inject.utils.Parameters.notNull;

import javax.inject.Provider;

import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.Key;
import org.usrz.libs.inject.ProvisionException;
import org.usrz.libs.inject.TypeLiteral;

/**
 * A {@link Provider} for instances of objects delegating creation to an
 * {@link Injector}.
 * <p>
 * This is the <em>normal</em> type of any provider that is required as
 * a dependency for an object.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public final class InjectorProvider<T> implements Provider<T> {

    private final Injector injector;
    private final Key<T> key;

    /**
     * Create a new {@link InjectorProvider} given the specified
     * {@link Injector} and {@link Key}.
     */
    public InjectorProvider(Injector injector, Key<T> key) {
        this.injector = notNull(injector, "Null injector");
        this.key = notNull(key, "Null key");
    }

    /**
     * Create a new {@link InjectorProvider} given the specified
     * {@link Injector} and {@link Key}.
     * <p>
     * This is provided as a <em>static</em> method in order to cope with
     * casting and generics.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> T create(Injector injector, Key<?> key) {
        /*
         * NOTE: we create this without checking if anything is bound in
         * the Injector itself: this provider will resolve dependency on
         * get() and possibly create dynamic bindings
         */
        if (! key.isProviderKey())
            throw new ProvisionException("Key must be associated with a provider", key);

        final TypeLiteral<?> type = notNull(key, "Null key")
                                        .getTypeLiteral()
                                        .getParameters()
                                        .get(0);
        return (T) new InjectorProvider(injector, key.with(type));
    }

    @Override
    public T get() {
        return injector.getInstance(key);
    }
}
