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
package org.usrz.libs.riak.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.usrz.libs.riak.annotations.RiakIndex.Type.AUTODETECT;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.usrz.libs.riak.IndexType;

@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface RiakIndex {

    public enum Type {
        AUTODETECT(null),
        BINARY(IndexType.BINARY),
        INTEGER(IndexType.INTEGER);

        private final IndexType type;

        private Type(IndexType type) {
            this.type = type;
        }

        public IndexType getIndexType() {
            if (type != null) return type;
            throw new IllegalStateException("No type for " + this);
        }

    };

    public String value() default "";

    public String name() default "";

    public Type type() default AUTODETECT;

}
