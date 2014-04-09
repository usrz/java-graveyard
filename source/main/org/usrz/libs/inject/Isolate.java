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

import org.usrz.libs.configurations.Configurations;

/**
 * A simple utility class to create and manage isolates (binding trees).
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public abstract class Isolate {

    /** The current (isolated) {@link Binder}. */
    protected final Binder binder;
    /** The parent {@link Binder}. */
    protected final Binder parent;

    /**
     * Create a new {link Isolate} from the specified {@link Binder}.
     */
    protected Isolate(Binder binder) {
        parent = notNull(binder, "Null binder");
        this.binder = binder.isolate();
    }

    /**
     * A convenience method configuring the current (child) {@link Binder}.
     */
    public void configure(Configurations configurations) {
        binder.configure(configurations);
    }

}
