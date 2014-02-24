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

public final class Key implements RiakLocation {

    private static final Pattern PATTERN = Pattern.compile("(.*)/(riak|buckets)/([^/]+)/(keys/)?([^/\\?]+)([/\\?].*)?");

    private final RiakClient client;
    private final Bucket bucket;
    private final String key;

    public Key(RiakClient client, String location) {
        if (client == null) throw new NullPointerException("Null client");
        final Matcher matcher = PATTERN.matcher(location);
        if (matcher.matches()) {
            this.client = client;
            bucket = client.getBucket(RiakUtils.decode(matcher.group(3)));
            key = RiakUtils.decode(matcher.group(5));
            if (key == null) throw new NullPointerException("Null key parsing: " + location);
            if (key.length() == 0) throw new IllegalArgumentException("Empty key parsing: " + location);
        } else {
            throw new IllegalArgumentException("Invalid link location: " + location);
        }
    }

    public Key(Bucket bucket, String key) {
        if (bucket == null) throw new NullPointerException("Null bucket");
        if (key == null) throw new NullPointerException("Null key");
        if (key.length() == 0) throw new IllegalArgumentException("Empty key");

        client = bucket.getRiakClient();
        this.bucket = bucket;
        this.key = key;
    }

    public Key(RiakClient client, String bucket, String key) {
        this(client.getBucket(bucket), key);
    }

    /* ====================================================================== */

    @Override
    public final RiakClient getRiakClient() {
        return client;
    }

    public final Bucket getBucket() {
        return bucket;
    }

    public final String getBucketName() {
        return bucket.getName();
    }

    public final String getName() {
        return key;
    }

    @Override
    public final String getLocation() {
        return bucket.getLocation() + "keys/" + RiakUtils.encode(key);
    }

    /* ====================================================================== */

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        try {
            final Key key = (Key) object;
            return bucket.equals(key.bucket) && this.key.equals(key.key);
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
                         .append('[')
                         .append(getLocation())
                         .append("]@")
                         .append(Integer.toHexString(hashCode()))
                         .toString();
    }
}
