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

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.Linker;
import org.usrz.libs.inject.TypeLiteral;
import org.usrz.libs.inject.bind.Resolver;

/**
 * A {@link Resolver} delegating object creation to the specified
 * {@link Provider}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class ProviderResolver<T> implements Resolver<T> {

    private final Resolver<? extends Provider<? extends T>> delegate;

    /**
     * Create a {@link ProviderResolver} associated with a {@link Provider}.
     * <p>
     * The {@link Provider} instance to use is resolved using the specified
     * <em>delegate</em> {@link Resolver} which should be either an
     * {@link InstanceResolver} or a {@link InjectingResolver}.
     *
     * @see Linker#toProvider(Class)
     * @see Linker#toProvider(Provider)
     * @see Linker#toProvider(TypeLiteral)
     */
    public ProviderResolver(Resolver<? extends Provider<? extends T>> delegate) {
        this.delegate = notNull(delegate, "Null provider resolver delegate");
    }

    @Override
    public T resolve(Injector injector)
    throws InjectionException {
        return delegate.resolve(injector).get();
    }

}
