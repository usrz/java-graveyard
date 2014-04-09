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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * A <em>marker annotation</em> used in conjunction with {@link Inject}
 * specifying that the needed dependency is <em>optional</em>, henceforth
 * the {@link Injector} will not attempt to dynamically create bindings.
 * <p>
 * When applied to a field, this annotation will prevent its value to be set
 * if no dependency was bound for it (henceforth preserving any default value
 * it was initialized with).
 * <p>
 * When applied to a method and/or constructor parameter, undefined values
 * will be replaced by <em>null</em>s (or the correct primitive value for it).
 * <p>
 * When applied to a method, <em>all of its parameters</em> will inherit
 * the annotation, and the method itself will <em>not</em> be invoked if
 * none of its parameters are bound. If <em>any</em> of its parameters is
 * bound, then the method will be invoked, and all undefined parameters
 * will be replaced by <em>null</em>.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface ProvidedBy {

    public Class<? extends Provider<?>> value();

}
