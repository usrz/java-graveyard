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

import static org.usrz.libs.riak.IndexType.INTEGER;

import java.util.Set;

import org.usrz.libs.riak.utils.MultiValueMap;

public class IndexMap extends MultiValueMap<Index, String> {

    public IndexMap() {
        super();
    }

    public IndexMap(IndexMap map) {
        putAll(map);
    }

    /* ====================================================================== */

    @Override
    protected Index validateKey(Index key) {
        if (key == null) throw new NullPointerException("Null keys not supported");
        return key;
    }

    @Override
    protected String validateValue(Index key, String value) {
        if (key == null) throw new NullPointerException("Null keys not supported");
        if (value == null) throw new NullPointerException("Null values not supported");

        /* Trim the string */
        final String string = value.trim();

        /* Check for numbers */
        if (key.getType() == INTEGER) {
            for (char c: string.toCharArray()) {
                if ((c >= '0') && (c <= '9')) continue;
                throw new IllegalArgumentException("Non-numeric value \"" + value + "\" for index " + key);
            }
        }

        /* Check that we *HAVE* something */
        if (string.length() > 0) return string;

        /* Empty string */
        throw new IllegalArgumentException("Invalid empty value for index " + key);
    }

    /* ====================================================================== */

    public Set<String> get(String name, IndexType type) {
        return get(new Index(name, type));
    }

    /* ====================================================================== */

    public boolean add(String name, IndexType type, Set<String> values) {
        return add(new Index(name, type), values);
    }

    public boolean add(String name, IndexType type, String value) {
        return add(new Index(name, type), value);
    }

    public boolean containsKey(String name, IndexType type) {
        return containsKey(new Index(name, type));
    }

    public boolean containsValue(String name, IndexType type, Object value) {
        return containsValue(new Index(name, type), value);
    }

    public Set<String> put(String name, IndexType type, Set<String> values) {
        return put(new Index(name, type), values);
    }

    public Set<String> put(String name, IndexType type, String value) {
        return put(new Index(name, type), value);
    }

    public Set<String> remove(String name, IndexType type) {
        return remove(new Index(name, type));
    }

    public boolean remove(String name, IndexType type, Object value) {
        return remove(new Index(name, type), value);
    }

    public boolean removeAll(String name, IndexType type, Set<Object> values) {
        return removeAll(new Index(name, type), values);
    }

}
