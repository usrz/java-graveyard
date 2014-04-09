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

import static org.usrz.libs.inject.utils.Parameters.notNull;

import java.lang.annotation.Annotation;
import java.util.UUID;

import javax.inject.Named;

/**
 * Utility to create {@link Named} annotations.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public final class Names {

    private Names() {
        throw new IllegalStateException("Do not construct");
    }

    /**
     * Create a new {@link Named} annotation with the specified value.
     */
    public static final Named named(String name) {
        return new NamedImpl(name);
    }

    /**
     * Create a new {@link Named} annotation with a unique value.
     */
    public static final Named unique() {
        return new NamedImpl(UUID.randomUUID().toString());
    }

    /* ====================================================================== */

    @SuppressWarnings("all")
    private static final class NamedImpl implements Named {

        private final String name;

        private NamedImpl(String name) {
            this.name = notNull(name, "Null name");
        }

        @Override
        public String value() {
            return name;
        }

        @Override
        public int hashCode() {
            return (127 * "value".hashCode()) ^ name.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) return true;
            if (object == null) return false;
            try {
                return name.equals(((Named) object).value());
            } catch (ClassCastException exception) {
                return false;
            }
        }

        @Override
        public String toString() {
            return "@" + Named.class.getName() + "(value=" + name + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Named.class;
        }
    }
}
