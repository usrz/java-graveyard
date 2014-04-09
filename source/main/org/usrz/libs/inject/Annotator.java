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
import static org.usrz.libs.inject.utils.Names.named;

import java.lang.annotation.Annotation;

import javax.inject.Named;

/**
 * An {@link Linker} capable of also modifying annotations for a bound
 * {@link Key}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <T> The type of binding this {@link Linker} refers to.
 */
public interface Annotator<T>
extends Linker<T> {

    /**
     * Add the specified annotation.
     */
    public Linker<T> with(Annotation annotation);

    /**
     * Add the specified annotation type (which must be a <em>marker</em>).
     */
    public Linker<T> with(Class<? extends Annotation> annotationType);

    /**
     * Add a {@link Named} annotation.
     */
    default Linker<T> with(String name) {
        return this.with(named(notNull(name, "Null name")));
    }

}