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
package org.usrz.libs.riak.introspection;

import org.usrz.libs.riak.Key;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class RiakEntityWriter<T> {

    private final BeanDescription description;

    private final KeySeri

    protected RiakEntityWriter(BeanDescription description) {
        this.description = description;

        for (BeanPropertyDefinition property: description.findProperties()) {
            if (property.couldSerialize()) {

            }
            System.err.println(property);
        }
    }

    public RiakEntityWriter<T> writeKey(T object, Key key) {
        description.
        return this;
    }

}
