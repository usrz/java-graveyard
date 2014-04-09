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

import static org.usrz.libs.inject.utils.Parameters.notNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.usrz.libs.inject.Key;
import org.usrz.libs.inject.ProvisionException;

/**
 * A simple collection of {@link Binding}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public final class Bindings implements Iterable<Binding<?>> {

    private final Map<Key<?>, Binding<?>> bindings;
    private final Scoping scoping;

    /**
     * Create a {@link Binding} instance associated with the
     * <b>null</b> {@link Scoping}.
     */
    public Bindings(Iterable<Binding<?>> bindings) {
        this(null, bindings);
    }

    /**
     * Create a {@link Binding} instance associated with the
     * specified {@link Scoping}.
     */
    public Bindings(Scoping scoping, Iterable<Binding<?>> bindings) {
        final Map<Key<?>, Binding<?>> map = new HashMap<>();
        notNull(bindings, "Null bindings").forEach((binding) -> {
            final Key<?> key = notNull(binding, "Null binding").getKey();
            final Binding<?> previous = map.put(key, binding);
            if (previous == null) return;
            throw new ProvisionException("Multiple bindings associated with the same key", key, previous, binding);
        });
        this.bindings = Collections.unmodifiableMap(map);
        this.scoping = scoping;
    }

    /**
     * Return the {@link Binding} associated with the specified {@link Key}
     * or <b>null</b>.
     */
    @SuppressWarnings("unchecked")
    public <T> Binding<T> getBinding(Key<T> key) {
        return (Binding<T>) bindings.get(key);
    }

    /**
     * Return the {@link Scoping} associated with this instance or <b>null</b>.
     */
    public Scoping getScoping() {
        return scoping;
    }

    /**
     * Iterate over all {@link Binding}s contained by this instance.
     */
    @Override
    public Iterator<Binding<?>> iterator() {
        return bindings.values().iterator();
    }

}
