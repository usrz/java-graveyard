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
package org.usrz.libs.riak;

import org.usrz.libs.riak.utils.MultiValueMap;

public class Metadata extends MultiValueMap<String, String> {

    public Metadata() {
        super();
    }

    public Metadata(Metadata map) {
        putAll(map);
    }

    @Override
    protected String validateKey(String key) {
        if (key == null) throw new NullPointerException("Null keys not supported");

        final String validated = key.trim().toLowerCase();
        if (validated.length() > 0) return validated;
        throw new IllegalArgumentException("Invalid empty key " + key);
    }

    @Override
    protected String validateValue(String key, String value) {
        if (key == null) throw new NullPointerException("Null keys not supported");
        if (value == null) throw new NullPointerException("Null values not supported");

        final String validated = value.trim();
        if (validated.length() > 0) return validated;
        throw new IllegalArgumentException("Invalid empty value " + key);
    }

}
