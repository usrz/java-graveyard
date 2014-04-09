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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * A <em>marker annotation</em> used in conjunction with {@link Inject}
 * specifying that the needed dependency is can be <b>null</b>.
 * <p>
 * Normally <b>null</b>s returned by {@link Provider}s will never be injected
 * in an instance. This annotation alters this behavior by allowing
 * <b>null</b>s to be injected into fields and method/constructor parameters.
 * <p>
 * Note that this annotation differs from {@linkplain Optional}, as in
 * dependencies will <em>still be resolved</em> and set normally as if they
 * were not <b>null</b>.
 * <p>
 * It goes without saying that it is an <em>error</em> to annotate with
 * {@link Nullable} dependencies whose type is a
 * {@linkplain Class#isPrimitive() primitive}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER })
public @interface Nullable {

}
