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
import java.util.StringTokenizer;

import org.usrz.libs.riak.utils.MultiValueMapBuilder;
import org.usrz.libs.riak.utils.RiakUtils;

public class MetadataBuilder extends MultiValueMapBuilder<String, String, Metadata, MetadataBuilder> {

    public MetadataBuilder() {
        this(null);
    }

    public MetadataBuilder(Metadata map) {
        super(map == null ? new Metadata() : map);
    }

    /* ====================================================================== */

    public MetadataBuilder parseHeader(String name, String value) {
        name = name.toLowerCase().trim();
        if (name.startsWith("x-riak-meta-")) {
            final String field = name.substring(12);
            final StringTokenizer tokenizer = new StringTokenizer(value, ", ", false);
            while (tokenizer.hasMoreTokens()) {
                this.add(field, RiakUtils.decode(tokenizer.nextToken().trim()));
            }
        }
        return this;
    }

    public MetadataBuilder parseHeaders(Map<String, ? extends Iterable<String>> headers) {
        if (headers == null) return this;
        for (Entry<String, ? extends Iterable<String>> header: headers.entrySet()) {
            final String name = header.getKey();
            for (String value: header.getValue()) {
                parseHeader(name, value);
            }
        }
        return this;
    }

}
