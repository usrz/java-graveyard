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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.usrz.libs.inject.bind.Binding;

/**
 * An exception thrown whenever an {@link Injector} encountered an error
 * while providing an object {@linkplain Injector#getInstance(Key) instance}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class ProvisionException extends IllegalStateException {

    public ProvisionException(String message, Key<?> dependency, Binding<?> binding) {
        super(prepareMessage(message, dependency, Collections.singleton(binding)));
    }

    public ProvisionException(String message, Key<?> dependency, Binding<?>... bindings) {
        super(prepareMessage(message, dependency, bindings == null ? null : Arrays.asList(bindings)));
    }

    public ProvisionException(String message, Key<?> dependency, Collection<Binding<?>> bindings) {
        super(prepareMessage(message, dependency, bindings));
    }

    public ProvisionException(String message, Key<?> dependency, Throwable cause, Binding<?> binding) {
        super(prepareMessage(message, dependency, Collections.singleton(binding)), cause);
    }

    public ProvisionException(String message, Key<?> dependency, Throwable cause, Binding<?>... bindings) {
        super(prepareMessage(message, dependency, bindings == null ? null : Arrays.asList(bindings)), cause);
    }

    public ProvisionException(String message, Key<?> dependency, Throwable cause, Collection<Binding<?>> bindings) {
        super(prepareMessage(message, dependency, bindings), cause);
    }

    private static String prepareMessage(String message, Key<?> dependency, Collection<Binding<?>> bindings) {
        final StringBuilder builder = new StringBuilder(message)
                                                .append("\n  Dependency: ")
                                                .append(dependency);

        String prefix = "\n  Binding: ";
        if (bindings != null) for (Binding<?> binding: bindings) {
            if (binding == null) continue;

            builder.append(prefix).append(binding.getKey())
                   .append("\n           - ");
            prefix = "\n           ";

            final String source = binding.getSource();
            builder.append("declared at ")
                   .append(source == null ? "(unknown source)" : source);
        }

        return builder.toString();
    }
}
