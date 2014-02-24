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

public class LinksMap extends MultiValueMap<String, Reference>
implements RiakClientAware {

    private final RiakClient client;

    public LinksMap(RiakClient client) {
        if (client == null) throw new NullPointerException("Null client");
        this.client = client;
    }

    public LinksMap(LinksMap map) {
        client = map.getRiakClient();
        putAll(map);
    }

    /* ====================================================================== */

    @Override
    public RiakClient getRiakClient() {
        return client;
    }

    /* ====================================================================== */

    @Override
    protected String validateKey(String key) {
        if (key == null) throw new NullPointerException("Null keys not supported");

        final String validated = key.trim().toLowerCase();
        if (validated.length() > 0) return validated;
        throw new IllegalArgumentException("Invalid empty key " + key);
    }

    @Override
    protected Reference validateValue(String key, Reference value) {
        if (key == null) throw new NullPointerException("Null keys not supported");
        if (value == null) throw new NullPointerException("Null values not supported");
        return value;
    }

    /* ====================================================================== */

    public boolean add(String tag, String bucket, String key) {
        return add(tag, new Reference(client, bucket, key));
    }

    public boolean add(String tag, Bucket bucket, String key) {
        return add(tag, new Reference(bucket, key));
    }

    public boolean containsValue(Object tag, String bucket, String key) {
        return containsValue(tag, new Reference(client, bucket, key));
    }

    public boolean containsValue(Object tag, Bucket bucket, String key) {
        return containsValue(tag, new Reference(bucket, key));
    }

    public void put(String tag, String bucket, String key) {
        put(tag, new Reference(client, bucket, key));
    }

    public void put(String tag, Bucket bucket, String key) {
        put(tag, new Reference(bucket, key));
    }

    public boolean remove(Object tag, String bucket, String key) {
        return remove(tag, new Reference(client, bucket, key));
    }

    public boolean remove(Object tag, Bucket bucket, String key) {
        return remove(tag, new Reference(bucket, key));
    }

}
