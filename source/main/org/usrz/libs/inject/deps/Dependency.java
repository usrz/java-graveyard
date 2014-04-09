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

import static org.usrz.libs.inject.utils.Parameters.notNull;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.Key;
import org.usrz.libs.inject.Nullable;
import org.usrz.libs.inject.Optional;
import org.usrz.libs.inject.bind.Resolver;

/**
 * A {@link Dependency} identifies a {@link Resolver} of a dependency
 * identified by a {@link Descriptor} for a {@link Key}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public final class Dependency<T> implements Resolver<T> {

    private final boolean optional;
    private final boolean nullable;
    private final Key<T> key;

    /**
     * Create a new {@link Dependency} instance.
     */
    public Dependency(Key<T> key, boolean optional, boolean nullable) {
        this.key = notNull(key, "Null key");
        this.optional = optional;
        this.nullable = nullable;
    }

    /**
     * Return the {@link Key} of this {@link Dependency}.
     */
    public final Key<T> getKey() {
        return key;
    }

    /**
     * Checks whether this {@link Dependency} is <em>optional</em>.
     *
     * @see Optional
     */
    public final boolean isOptional() {
        return optional;
    }

    /**
     * Checks whether this {@link Dependency} is <em>nullable</em>.
     *
     * @see Nullable
     */
    public final boolean isNullable() {
        return nullable;
    }

    @Override
    public final T resolve(Injector injector)
    throws InjectionException {
        final T instance = optional ?
                               injector.isBound(key) ?
                                   injector.getInstance(key) :
                                   null :
                               injector.getInstance(key);

        if (instance != null) return instance;
        if (optional || nullable) return null;
        throw new InjectionException("Dependency evaluated to \"null\"", key);
    }
}
