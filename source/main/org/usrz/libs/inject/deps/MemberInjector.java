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
package org.usrz.libs.inject.deps;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;

/**
 * An interface defining a {@link Descriptor} capable of resolving
 * {@linkplain Dependency dependencies} and injecting them in an existing
 * instance.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public interface MemberInjector<T> extends Descriptor<T> {

    /**
     * Resolve this {@link Descriptor}'s dependencies using the specified
     * {@link Injector} and inject them in the specified object instance.
     */
    public void inject(Injector injector, Object object)
    throws InjectionException;

}
