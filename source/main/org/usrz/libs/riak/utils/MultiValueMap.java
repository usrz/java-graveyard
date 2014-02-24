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

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class MultiValueMap<K, V>
extends AbstractMap<K, Set<V>> {

    private final Map<K, Set<V>> delegates = new HashMap<>();

    protected MultiValueMap() {
        /* Nothing to do... */
    }

    /* ====================================================================== */

    private Set<V> create(K key) {
        Set<V> set = delegates.get(key);
        if (set == null) delegates.put(key, set = new HashSet<>());
        return set;
    }

    /* ====================================================================== */

    protected abstract K validateKey(K key);

    protected abstract V validateValue(K key, V value);

    /* ====================================================================== */

    @Override
    public final int size() {
        return delegates.size();
    }

    @Override
    public final boolean isEmpty() {
        return delegates.size() == 0;
    }

    @Override
    public final void clear() {
        delegates.clear();
    }

    /* ====================================================================== */

    @Override
    public final boolean containsKey(Object key) {
        return delegates.containsKey(key);
    }

    @Override
    public final boolean containsValue(Object value) {
        return delegates.containsValue(value);
    }

    public final boolean containsValue(Object key, Object value) {
        final Set<V> values = delegates.get(key);
        return values == null ? false : values.contains(value);
    }

    /* ====================================================================== */

    public final boolean add(K key, V value) {
        return add(key, Collections.singleton(value));
    }

    public final boolean add(K key, Set<V> values) {
        final Set<V> delegate = create(validateKey(key));

        boolean result = false;
        if (values != null) for (V value: values)
            result |= delegate.add(validateValue(key, value));
        return result ;
    }

    public final void addAll(Map<? extends K, ? extends Set<V>> map) {

        /* Do not check keys/values if we're on the same class */
        if (this.getClass().equals(map.getClass())) {
            @SuppressWarnings("unchecked")
            final MultiValueMap<K, V> multiValueMap = (MultiValueMap<K, V>) map;
            for (Entry<K, Set<V>> entry: multiValueMap.delegates.entrySet())
                this.create(entry.getKey()).addAll(entry.getValue());
            return;
        }

        if (map != null) {
            for (Entry<? extends K, ? extends Set<V>> entry: map.entrySet()) {
                this.add(entry.getKey(), entry.getValue());
            }
        }
    }

    /* ====================================================================== */

    public final Set<V> put(K key, V value) {
        return this.put(key, Collections.singleton(validateValue(key, value)));
    }

    @Override
    public final Set<V> put(K key, Set<V> values) {
        key = validateKey(key);
        if ((values != null) && (values.size() > 0)) {
            final Set<V> newValues = new HashSet<>();
            for (V value: values)
                newValues.add(validateValue(key, value));
            return delegates.put(key, newValues);
        } else {
            return delegates.remove(key);
        }
    }

    @Override
    public final void putAll(Map<? extends K, ? extends Set<V>> map) {
        /* Do not check keys/values if we're on the same class */
        if (this.getClass().equals(map.getClass())) {
            @SuppressWarnings("unchecked")
            final MultiValueMap<K, V> multiValueMap = (MultiValueMap<K, V>) map;
            for (Entry<K, Set<V>> entry: multiValueMap.delegates.entrySet())
                this.delegates.put(entry.getKey(), new HashSet<V>(entry.getValue()));
            return;
        }

        /* Slow method, check everything */
        if (map != null) {
            for (Entry<? extends K, ? extends Set<V>> entry: map.entrySet()) {
                this.put(entry.getKey(), entry.getValue());
            }
        }

    }

    /* ====================================================================== */

    @Override
    public final Set<V> remove(Object key) {
        return delegates.remove(key);
    }

    public final boolean remove(Object key, Object value) {
        final Set<V> delegate = this.delegates.get(key);
        if (delegate == null) return false;
        try {
            return delegate.remove(value);
        } finally {
            if (delegate.isEmpty()) delegates.remove(key);
        }
    }

    public final boolean removeAll(Object key, Set<Object> values) {
        final Set<V> delegate = this.delegates.get(key);
        if (delegate == null) return false;
        try {
            return delegate.removeAll(values);
        } finally {
            if (delegate.isEmpty()) delegates.remove(key);
        }
    }

    /* ====================================================================== */
    /* KEY SET IMPLEMENTATION                                                 */
    /* ====================================================================== */

    @Override
    public final Set<K> keySet() {
        return delegates.keySet();
    }

    /* ====================================================================== */
    /* VALUES COLLECTION IMPLEMENTATION                                       */
    /* ====================================================================== */

    @Override
    public final Collection<Set<V>> values() {
        return new Values();
    }

    private class Values extends AbstractCollection<Set<V>> {

        @Override
        public final Iterator<Set<V>> iterator() {
            return new ValuesIterator(keySet().iterator());
        }

        @Override
        public final int size() {
            return delegates.size();
        }
    }

    private class ValuesIterator implements Iterator<Set<V>> {

        private final Iterator<K> iterator;

        private ValuesIterator(Iterator<K> iterator) {
            this.iterator = iterator;
        }

        @Override
        public final boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public final Set<V> next() {
            return get(iterator.next());
        }

        @Override
        public final void remove() {
            iterator.remove();
        }
    }

    /* ====================================================================== */
    /* ENTRY SET IMPLEMENTATION                                               */
    /* ====================================================================== */

    @Override
    public final Set<Entry<K, Set<V>>> entrySet() {
        return new EntrySet();
    }

    private class EntrySet extends AbstractSet<Entry<K, Set<V>>> {

        @Override
        public final Iterator<Entry<K, Set<V>>> iterator() {
            return new EntrySetIterator(keySet().iterator());
        }

        @Override
        public final int size() {
            return delegates.size();
        }

    }

    private class EntrySetIterator implements Iterator<Entry<K, Set<V>>> {

        private final Iterator<K> iterator;

        private EntrySetIterator(Iterator<K> iterator) {
            this.iterator = iterator;
        }

        @Override
        public final boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public final Entry<K, Set<V>> next() {
            return new EntrySetEntry(iterator.next());
        }

        @Override
        public final void remove() {
            iterator.remove();
        }
    }

    private class EntrySetEntry implements Entry<K, Set<V>> {

        private final K key;

        private EntrySetEntry(K key) {
            this.key = key;
        }

        @Override
        public final K getKey() {
            return key;
        }

        @Override
        public final Set<V> getValue() {
            return get(key);
        }

        @Override
        public final Set<V> setValue(Set<V> value) {
            return put(key, value);
        }
    }

    /* ====================================================================== */
    /* MAPPED VALUES IMPLEMENTATION                                           */
    /* ====================================================================== */

    @Override
    @SuppressWarnings("unchecked")
    public final Set<V> get(Object key) {
        return new MappedValues(validateKey((K)key));
    }

    private class MappedValues extends AbstractSet<V> {

        private final K key;

        private MappedValues(K key) {
            this.key = key;
        }

        /* ================================================================== */

        @Override
        public final boolean contains(Object value) {
            return MultiValueMap.this.containsValue(key, value);
        }

        @Override
        public final boolean add(V value) {
            return MultiValueMap.this.add(key, value);
        }

        @Override
        public final boolean remove(Object value) {
            return MultiValueMap.this.remove(key, value);
        }

        @Override
        public final Iterator<V> iterator() {
            final Set<V> delegate = delegates.get(key);
            if (delegate == null) return Collections.<V>emptyIterator();
            return new MappedValuesIterator(key, delegate.iterator());
        }

        @Override
        public final void clear() {
            MultiValueMap.this.remove(key);
        }

        @Override
        public final int size() {
            final Set<V> delegate = delegates.get(key);
            return delegate == null ? 0 : delegate.size();
        }
    }

    private class MappedValuesIterator implements Iterator<V> {

        private final K key;
        private final Iterator<V> iterator;
        private V last = null;

        private MappedValuesIterator(K key, Iterator<V> iterator) {
            this.key = key;
            this.iterator = iterator;
        }

        @Override
        public final boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public final V next() {
            return last = iterator.next();
        }

        @Override
        public final void remove() {
            MultiValueMap.this.remove(key, last);
        }
    }
}
