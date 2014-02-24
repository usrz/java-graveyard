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


public enum IndexType {

    BINARY("_bin"), INTEGER("_int");

    private final String suffix;

    private IndexType(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public static Index parseHeaderName(final String header) {
        final String normalized = header.toLowerCase();
        final int start = normalized.startsWith("x-riak-index-") ? 13 : 0;

        for (IndexType type: IndexType.values()) {
            if (type.suffix == null) continue;
            if (!normalized.endsWith(type.suffix)) continue;
            final int end = normalized.length() - type.suffix.length();
            final String name = normalized.substring(start, end);
            if (name.length() == 0) continue;
            return new Index(name, type);
        }

        throw new IllegalArgumentException("Invalid Riak index name \"" + header + "\"");
    }

}
