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

import static org.usrz.libs.riak.IndexType.BINARY;
import static org.usrz.libs.riak.IndexType.INTEGER;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.usrz.libs.riak.Bucket;
import org.usrz.libs.riak.IndexMap;
import org.usrz.libs.riak.LinksMap;
import org.usrz.libs.riak.Metadata;
import org.usrz.libs.riak.Reference;
import org.usrz.libs.riak.annotations.RiakBucket;
import org.usrz.libs.riak.annotations.RiakIndex;
import org.usrz.libs.riak.annotations.RiakKey;
import org.usrz.libs.riak.annotations.RiakLink;
import org.usrz.libs.riak.annotations.RiakMetadata;
import org.usrz.libs.riak.annotations.RiakVectorClock;
import org.usrz.libs.utils.beans.IntrospectedProperty;
import org.usrz.libs.utils.beans.IntrospectionDescriptor;
import org.usrz.libs.utils.beans.IntrospectionException;
import org.usrz.libs.utils.beans.Introspector;

public class RiakIntrospector {

    private final Introspector introspector;

    public RiakIntrospector() {
        this (new Introspector());
    }

    public RiakIntrospector(Introspector introspector) {
        if (introspector == null) throw new NullPointerException("Null introspector");
        this.introspector = introspector;
    }

    /* ====================================================================== */

    @SuppressWarnings("unchecked")
    private <T> IntrospectionDescriptor<T> descriptor(T instance) {
        if (instance == null) throw new NullPointerException("Null instance");
        return (IntrospectionDescriptor<T>) introspector.getDescriptor(instance.getClass());
    }

    private Set<Object> combine(Set<Object> result, Object object) {
        if (object == null) return result;

        if (object instanceof Iterable) {
            for (Object o: ((Iterable<?>) object)) combine(result, o);
        } else if (object.getClass().isArray()) {
            final int l = Array.getLength(object);
            for (int x = 0; x < l; x ++) combine(result, Array.get(object, x));
        } else {
            result.add(object);
        }
        return result;
    }

    private Set<Object> combine(Object object) {
        return combine(new HashSet<Object>(), object);
    }

    /* ====================================================================== */

    public <T> String getKey(T instance) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);

        for (Entry<RiakKey, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakKey.class).entrySet()) {
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canRead()) return property.read(instance, String.class);
            }
        }

        return null;
    }

    public <T> String getBucket(T instance) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);

        for (Entry<RiakBucket, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakBucket.class).entrySet()) {
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canRead()) try {
                    return property.read(instance, Bucket.class).getName();
                } catch (IntrospectionException exception) {
                    return property.read(instance, String.class);
                }
            }
        }

        return null;
    }

    public <T> Reference getReference(T instance) {
        try {
            return new Reference(this.getBucket(instance), this.getKey(instance));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to construct reference from " + instance.getClass().getName() + ": " + exception.getMessage(), exception);
        }
    }

    public <T> String getVectorClock(T instance) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);

        for (Entry<RiakVectorClock, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakVectorClock.class).entrySet()) {
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canRead()) return property.read(instance, String.class);
            }
        }

        return null;
    }

    public <T> Metadata getMetadata(T instance) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);
        final Metadata metadata = new Metadata();

        for (Entry<RiakMetadata, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakMetadata.class).entrySet()) {
            final RiakMetadata annotation = entry.getKey();
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canRead()) {
                    final String field = "".equals(annotation.value()) ? property.getName() : annotation.value();
                    if ((field == null) || ("".equals(field))) throw new IllegalStateException("Metadata field unknown reading " + property);

                    /* All values, one by one */
                    for (Object object: combine(property.readAll(instance))) {
                        metadata.add(field, object.toString());
                    }
                }
            }
        }

        return metadata;
    }

    public <T> IndexMap getIndexMap(T instance) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);
        final IndexMap indexMap = new IndexMap();

        for (Entry<RiakIndex, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakIndex.class).entrySet()) {
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canRead()) {

                    /* Hairy... prefer "name" over "value", and use property name as default */
                    final RiakIndex annotation = entry.getKey();
                    final String name = "".equals(annotation.name()) ?
                                            "".equals(annotation.value()) ?
                                                property.getName() :
                                                annotation.value() :
                                            annotation.name();
                    if ((name == null) || ("".equals(name))) throw new IllegalStateException("Index name unknown reading " + property);

                    for (Object object: combine(property.readAll(instance))) {
                        switch (annotation.type()) {
                            case AUTODETECT: indexMap.add(name, object instanceof Number ? INTEGER : BINARY,  object.toString()); break;
                            case INTEGER:    indexMap.add(name, INTEGER, object.toString()); break;
                            case BINARY:     indexMap.add(name, BINARY,  object.toString()); break;
                            default: throw new IllegalStateException("Unsupported index type " + annotation.type() + " reading " + property);
                        }
                    }
                }
            }
        }

        return indexMap;
    }

    public <T> LinksMap getLinksMap(T instance) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);
        final LinksMap linksMap = new LinksMap();

        for (Entry<RiakLink, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakLink.class).entrySet()) {
            final RiakLink annotation = entry.getKey();
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canRead()) {

                    /* Get the link tag */
                    final String tag = "".equals(annotation.value()) ? property.getName() : annotation.value();
                    if ((tag == null) || ("".equals(tag))) throw new IllegalStateException("Link tag unknown reading " + property);

                    /* Get the reference and add our link */
                    for (Object link: combine(property.readAll(instance))) {
                        if (link instanceof Reference) {
                            linksMap.add(tag, (Reference) link);
                        } else if (link instanceof String) {
                            linksMap.add(tag, new Reference((String) link));
                        } else {
                            linksMap.add(tag, getReference(link));
                        }
                    }
                }
            }
        }

        return linksMap;
    }
}
