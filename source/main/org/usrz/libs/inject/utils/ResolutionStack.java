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
package org.usrz.libs.inject.utils;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.usrz.libs.inject.Key;
import org.usrz.libs.inject.ProvisionException;
import org.usrz.libs.inject.bind.Binding;

/**
 * A very simple {@link ThreadLocal}-based stack-resolutor used to detect
 * circular dependencies while resolving instance.s
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class ResolutionStack {

    private final ThreadLocal<LinkedHashMap<Key<?>, Binding<?>>> localStack =
            new ThreadLocal<LinkedHashMap<Key<?>, Binding<?>>>() {
                @Override
                protected LinkedHashMap<Key<?>, Binding<?>> initialValue() {
                    return new LinkedHashMap<>();
                }
            };

    /**
     * Create a new {@link ResolutionStack}.
     */
    public ResolutionStack() {
        /* Nothing to do */
    }

    /**
     * Declare that we are starting to resolve the specified {@link Binding}.
     */
    public <T> Binding<T> enter(Binding<T> binding) {
        if (binding == null) return null;

        final Key<T> key = binding.getKey();
        final LinkedHashMap<Key<?>, Binding<?>> stack = localStack.get();

        if (stack.containsKey(binding.getKey()))
            throw new ProvisionException("Circular dependency detected", key, stack.values());

        stack.put(key, binding);
        return binding;
    }

    /**
     * Declare that we are done resolving the specified {@link Binding}.
     */
    public void exit(Binding<?> binding) {
        if (binding != null) localStack.get().remove(binding.getKey());
    }

    /**
     * Return all the {@link Binding}s in the current stack (in order).
     */
    public Collection<Binding<?>> getBindings() {
        return localStack.get().values();
    }

}
