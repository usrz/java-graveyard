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

import javax.inject.Singleton;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;

/**
 * A {@link Resolver} provides values of an Object using an {@link Injector}
 * to resolve its dependencies.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <T> The type of objects managed by this {@link Resolver}
 */
public interface Resolver<T> {

    /**
     * Create an instance (or return the {@link Singleton} instance) of the
     * object managed by this {@link Resolver}.
     */
    public T resolve(Injector injector)
    throws InjectionException;

}
