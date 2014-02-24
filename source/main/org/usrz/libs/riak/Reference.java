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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.usrz.libs.riak.utils.RiakUtils;

public class Reference {

    private static final Pattern PATTERN = Pattern.compile("(.*)/(riak|buckets)/([^/]+)/(keys/)?([^/\\?]+)([/\\?].*)?");
    private final String bucket;
    private final String key;

    public Reference(String location) {
        final Matcher matcher = PATTERN.matcher(location);
        if (matcher.matches()) {
            bucket = RiakUtils.decode(matcher.group(3));
            key = RiakUtils.decode(matcher.group(5));
            if (key == null) throw new NullPointerException("Null key parsing: " + location);
            if (bucket == null) throw new NullPointerException("Null bucket parsing: " + location);
            if (key.length() == 0) throw new IllegalArgumentException("Empty key parsing: " + location);
            if (bucket.length() == 0) throw new IllegalArgumentException("Empty bucket parsing: " + location);
        } else {
            throw new IllegalArgumentException("Invalid link location: " + location);
        }
    }

    public Reference(Bucket bucket, String key) {
        this(bucket.getName(), key);
    }

    public Reference(String bucket, String key) {
        if (bucket == null) throw new NullPointerException("Null bucket");
        if (key == null) throw new NullPointerException("Null key");
        if (bucket.length() == 0) throw new IllegalArgumentException("Empty bucket");
        if (key.length() == 0) throw new IllegalArgumentException("Empty key");
        this.bucket = bucket;
        this.key = key;
    }

    public final String getBucket() {
        return bucket;
    }

    public final String getKey() {
        return key;
    }

    public final String getLocation() {
        return "/buckets/" + RiakUtils.encode(bucket) + "/keys/" + RiakUtils.encode(key);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        try {
            final Reference index = (Reference) object;
            return bucket.equals(index.bucket) && key.equals(index.key);
        } catch (ClassCastException exception) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return bucket.hashCode() ^ key.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder(this.getClass().getName())
                         .append("[/buckets/")
                         .append(bucket)
                         .append("/keys/")
                         .append(key)
                         .append("]@")
                         .append(Integer.toHexString(hashCode()))
                         .toString();
    }
}
