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

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.usrz.libs.riak.utils.MultiValueMapBuilder;

public class LinksMapBuilder extends MultiValueMapBuilder<String, Reference, LinksMap, LinksMapBuilder> {

    private static final Pattern LINK_PATTERN = Pattern.compile("\\s*<([^>]+)>\\s*;\\s*riaktag\\s*=\\s*\"?([^\"]+)\"?\\s*");

    public LinksMapBuilder() {
        this(null);
    }

    public LinksMapBuilder(LinksMap map) {
        super(map == null ? new LinksMap() : map);
    }

    /* ====================================================================== */

    public LinksMapBuilder parseHeader(String header) {
        final StringTokenizer tokenizer = new StringTokenizer(header, ", ", false);
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            final Matcher matcher = LINK_PATTERN.matcher(token);
            if (matcher.matches()) add(matcher.group(2), new Reference(matcher.group(1)));
        }
        return this;
    }

    public LinksMapBuilder parseHeaders(Iterable<String> headers) {
        if (headers == null) return this;
        for (String value: headers) parseHeader(value);
        return this;
    }

    /* ====================================================================== */

    public LinksMapBuilder add(String tag, String bucket, String key) {
        return add(tag, new Reference(bucket, key));
    }

    public LinksMapBuilder add(String tag, Bucket bucket, String key) {
        return add(tag, new Reference(bucket, key));
    }

    public LinksMapBuilder put(String tag, String bucket, String key) {
        return put(tag, new Reference(bucket, key));
    }

    public LinksMapBuilder put(String tag, Bucket bucket, String key) {
        return put(tag, new Reference(bucket, key));
    }

    public LinksMapBuilder remove(Object tag, String bucket, String key) {
        return remove(tag, new Reference(bucket, key));
    }

    public LinksMapBuilder remove(Object tag, Bucket bucket, String key) {
        return remove(tag, new Reference(bucket, key));
    }

}
