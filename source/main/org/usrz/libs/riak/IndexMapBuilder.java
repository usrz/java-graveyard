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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.usrz.libs.riak.utils.MultiValueMapBuilder;
import org.usrz.libs.riak.utils.RiakUtils;

public class IndexMapBuilder extends MultiValueMapBuilder<Index, String, IndexMap, IndexMapBuilder> {

    public IndexMapBuilder() {
        this(null);
    }

    public IndexMapBuilder(IndexMap map) {
        super(map == null ? new IndexMap() : map);
    }

    /* ====================================================================== */

    public IndexMapBuilder parseHeader(String name, String value) {
        if (name.toLowerCase().trim().startsWith("x-riak-index-")) {
            final Index index = new Index(name);
            final StringTokenizer tokenizer = new StringTokenizer(value, ",", false);
            while (tokenizer.hasMoreTokens()) {
                this.add(index, RiakUtils.decode(tokenizer.nextToken().trim()));
            }
        }
        return this;
    }

    public IndexMapBuilder parseHeaders(Map<String, ? extends Iterable<String>> headers) {
        if (headers == null) return this;
        for (Entry<String, ? extends Iterable<String>> header: headers.entrySet()) {
            final String name = header.getKey();
            for (String value: header.getValue()) {
                parseHeader(name, value);
            }
        }
        return this;
    }

    /* ====================================================================== */

    public IndexMapBuilder add(String name, IndexType type, Set<String> values) {
        this.add(new Index(name, type), values);
        return this;
    }

    public IndexMapBuilder add(String name, IndexType type, String value) {
        this.add(new Index(name, type), value);
        return this;
    }

    public IndexMapBuilder put(String name, IndexType type, Set<String> values) {
        this.put(new Index(name, type), values);
        return this;
    }

    public IndexMapBuilder put(String name, IndexType type, String value) {
        this.put(new Index(name, type), value);
        return this;
    }

    public IndexMapBuilder remove(String name, IndexType type) {
        this.remove(new Index(name, type));
        return this;
    }

    public IndexMapBuilder remove(String name, IndexType type, Object value) {
        this.remove(new Index(name, type), value);
        return this;
    }

    public IndexMapBuilder removeAll(String name, IndexType type, Set<Object> values) {
        this.removeAll(new Index(name, type), values);
        return this;
    }
}
