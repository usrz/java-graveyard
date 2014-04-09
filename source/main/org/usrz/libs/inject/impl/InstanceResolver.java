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

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.bind.Resolver;

/**
 * A {@link Resolver} always returning the same instance of an object
 * (specified at construction), but managing its dependency injection (once).
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class InstanceResolver<T> implements Resolver<T> {

    private volatile boolean injected = false;
    private final T instance;

    /**
     * Create a new {@link InstanceResolver} with the specified instance.
     */
    public InstanceResolver(T instance) {
        this.instance = notNull(instance, "Null instance");
    }

    @Override
    public T resolve(Injector injector)
    throws InjectionException {
        if (injected) return instance;
        synchronized (this) {
            if (injected) return instance;
            injector.injectMembers(instance);
            injected = true;
        }
        return instance;
    }

}
