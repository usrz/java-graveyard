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
package org.usrz.libs.riak.utils;

import java.util.Map;
import java.util.Set;

public abstract class MultiValueMapBuilder<K,
                                           V,
                                           M extends MultiValueMap<K, V>,
                                           B extends MultiValueMapBuilder<K, V, M, B>> {
    private final B thisInstance;
    private final M map;

    @SuppressWarnings("unchecked")
    protected MultiValueMapBuilder(M map) {
        if (map == null) throw new NullPointerException("Null map");
        this.thisInstance = (B) this;
        this.map = map;
    }

    public M build() {
        return map;
    }

    public final B clear() {
        this.map.clear();
        return thisInstance;
    }

    public final B add(K key, Set<V> values) {
        this.map.add(key, values);
        return thisInstance;
    }

    public final B add(K key, V value) {
        this.map.add(key, value);
        return thisInstance;
    }

    public final B addAll(Map<? extends K, ? extends Set<V>> map) {
        this.map.addAll(map);
        return thisInstance;
    }

    public final B put(K key, Set<V> values) {
        this.map.put(key, values);
        return thisInstance;
    }

    public final B put(K key, V value) {
        this.map.put(key, value);
        return thisInstance;
    }

    public final B putAll(Map<? extends K, ? extends Set<V>> map) {
        this.map.putAll(map);
        return thisInstance;
    }

    public final B remove(Object key) {
        this.map.remove(key);
        return thisInstance;
    }

    public final B remove(Object key, Object value) {
        this.map.remove(key, value);
        return thisInstance;
    }

    public final B removeAll(Object key, Set<Object> values) {
        this.map.removeAll(key, values);
        return thisInstance;
    }

}
