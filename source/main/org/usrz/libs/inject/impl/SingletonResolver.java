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

import javax.inject.Singleton;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.bind.Resolver;

/**
 * A {@link Resolver} treating resolution of instances as {@link Singleton}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class SingletonResolver<T> implements Resolver<T> {

    private final Resolver<? extends T> delegate;
    private boolean resolved = false;
    private T instance = null;

    /**
     * Create a {@link SingletonResolver} wrapping another {@link Resolver}.
     */
    public SingletonResolver(Resolver<? extends T> delegate) {
        this.delegate = notNull(delegate, "Null provider resolver delegate");
    }

    @Override
    public T resolve(Injector injector)
    throws InjectionException {
        if (resolved) return (instance);
        synchronized(this) {
            if (resolved) return (instance);
            instance = delegate.resolve(injector);
            resolved = true;
        }
        return instance;
    }

}
