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

import java.util.List;

import org.usrz.libs.inject.TypeLiteral;

/**
 * The {@link Descriptor} interface describes an object type through its
 * {@link TypeLiteral} and associated {@linkplain Dependency dependencies}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <T> The type of the object this {@link Descriptor} describes.
 */
public interface Descriptor<T> {

    /**
     * Return the {@link TypeLiteral} associated with this instance.
     */
    public TypeLiteral<T> getTypeLiteral();

    /**
     * Return a {@link List} of {@linkplain Dependency dependencies}
     * associated with this {@link Descriptor}.
     */
    public List<Dependency<?>> getDependencies();

}
