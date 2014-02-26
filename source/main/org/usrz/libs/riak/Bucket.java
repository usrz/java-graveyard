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

import static java.lang.Integer.toHexString;

import java.io.IOException;

import org.usrz.libs.riak.utils.IterableFuture;
import org.usrz.libs.riak.utils.RiakUtils;

public class Bucket implements RiakLocation {

    private final RiakClient client;
    private final String name;

    protected Bucket(RiakClient client, String name) {
        if (name == null) throw new NullPointerException("Null bucket name");
        if (name.length() == 0) throw new IllegalArgumentException("Empty bucket name");
        if (client == null) throw new NullPointerException("Null client");
        this.client = client;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public RiakClient getRiakClient() {
        return client;
    }

    @Override
    public String getLocation() {
        return "/buckets/" + RiakUtils.encode(name) + "/";
    }

    public IterableFuture<String> getKeyNames()
    throws IOException {
        return client.getKeyNames(name);
    }

    public IterableFuture<Key> getKeys()
    throws IOException {
        return client.getKeys(name);
    }

    /* ====================================================================== */

    public <T> FetchRequest<T> fetch(String key, Class<T> type) {
        return client.fetch(name, key, type);
    }

    public <T> FetchRequest<T> fetch(String key, ResponseHandler<T> handler) {
        return client.fetch(name, key, handler);
    }

    public <T> StoreRequest<T> store(T object) {
        return client.store(name, object);
    }

    public <T> StoreRequest<T> store(T object, ResponseHandler<T> handler) {
        return client.store(name, object, handler);
    }

    public <T> StoreRequest<T> store(T object, String key) {
        return client.store(name, key, object);
    }

    public <T> StoreRequest<T> store(T object, String key, ResponseHandler<T> handler) {
        return client.store(name, key, object, handler);
    }

    public DeleteRequest delete(String key) {
        return client.delete(name, key);
    }

    /* ====================================================================== */

    @Override
    public int hashCode() {
        return client.hashCode() ^ name.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        try {
            final Bucket bucket = (Bucket) object;
            return (bucket.client == client) && name.equals(bucket.name);
        } catch (ClassCastException exception) {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + name + "]@" + toHexString(hashCode());
    }
}
