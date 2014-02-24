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

import org.usrz.libs.riak.utils.RiakUtils;

public class Index {

    private final String name;
    private final IndexType type;

    public Index(String header) {
        if (header == null) throw new NullPointerException("Null header name");

        final String normalized = header.toLowerCase().trim();
        final int start = normalized.startsWith("x-riak-index-") ? 13 : 0;

        for (IndexType type: IndexType.values()) {
            final String suffix = type.getSuffix();
            if (suffix == null) continue;
            if (!normalized.endsWith(suffix)) continue;
            final int end = normalized.length() - suffix.length();
            final String name = normalized.substring(start, end);
            if (name.length() == 0) continue;
            this.name = RiakUtils.decode(name);
            this.type = type;
            return;
        }

        throw new IllegalArgumentException("Invalid Riak index name \"" + header + "\"");
    }

    public Index(String name, IndexType type) {
        if (name == null) throw new NullPointerException("Null name");
        if (type == null) throw new NullPointerException("Null type");

        this.name = name.trim().toLowerCase();
        this.type = type;

        if (this.name.length() < 1)
            throw new IllegalArgumentException("Invalid index name " + name);
    }

    public final String getName() {
        return name;
    }

    public final IndexType getType() {
        return type;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        try {
            final Index index = (Index) object;
            return name.equals(index.name) && type.equals(index.type);
        } catch (ClassCastException exception) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ type.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder(this.getClass().getName())
                         .append('[')
                         .append(name)
                         .append(type.getSuffix())
                         .append("]@")
                         .append(Integer.toHexString(hashCode()))
                         .toString();
    }
}
