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
package org.usrz.libs.riak.annotations;

import static org.usrz.libs.riak.IndexType.BINARY;
import static org.usrz.libs.riak.IndexType.INTEGER;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.usrz.libs.riak.Bucket;
import org.usrz.libs.riak.IndexMap;
import org.usrz.libs.riak.IndexType;
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.LinksMap;
import org.usrz.libs.riak.Metadata;
import org.usrz.libs.riak.RiakClient;
import org.usrz.libs.utils.beans.IntrospectedProperty;
import org.usrz.libs.utils.beans.IntrospectionDescriptor;
import org.usrz.libs.utils.beans.IntrospectionException;
import org.usrz.libs.utils.beans.Introspector;

public class RiakIntrospector {

    private final Introspector introspector;
    private final RiakClient client;

    public RiakIntrospector(RiakClient client) {
        this (client, new Introspector());
    }

    public RiakIntrospector(RiakClient client, Introspector introspector) {
        if (introspector == null) throw new NullPointerException("Null introspector");
        if (client == null) throw new NullPointerException("Null client");
        this.introspector = introspector;
        this.client = client;
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

    protected String indexName(RiakIndex annotation, IntrospectedProperty<?> property) {
        /* Hairy... prefer "name" over "value", and use property name as default */
        final String name = "".equals(annotation.name()) ?
                "".equals(annotation.value()) ?
                    property.getName() :
                    annotation.value() :
                annotation.name();
        if (name == null) return null;
        final String normalized = name.toLowerCase().trim();
        return "".equals(normalized) ? null : name;
    }

    protected String linkTag(RiakLink annotation, IntrospectedProperty<?> property) {
        final String name = "".equals(annotation.value()) ? property.getName() : annotation.value();
        final String normalized = name.toLowerCase().trim();
        return "".equals(normalized) ? null : name;
    }

    protected String metadataField(RiakMetadata annotation, IntrospectedProperty<?> property) {
        final String name = "".equals(annotation.value()) ? property.getName() : annotation.value();
        final String normalized = name.toLowerCase().trim();
        return "".equals(normalized) ? null : name;
    }

    /* ====================================================================== */

    public <T> String getKeyName(T instance) {
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

    public <T> Key getKey(T instance) {
        try {
            return new Key(client, this.getBucket(instance), this.getKeyName(instance));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to construct key from " + instance.getClass().getName() + ": " + exception.getMessage(), exception);
        }
    }

    public <T> Metadata getMetadata(T instance) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);
        final Metadata metadata = new Metadata();

        for (Entry<RiakMetadata, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakMetadata.class).entrySet()) {
            final RiakMetadata annotation = entry.getKey();
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canRead()) {
                    final String field = metadataField(annotation, property);

                    if ((field == null) || ("".equals(field))) try {
                        final Metadata map = property.read(instance, Metadata.class);
                        if (map != null) metadata.addAll(map);
                        continue;
                    } catch (IntrospectionException exception) {
                        throw new IllegalStateException("Unnamed @RiakMetadata annotation should return a Metadata for" + property, exception);
                    }

                    /* All values, one by one */
                    for (Object object: combine(property.readAll(instance))) {
                        if (object instanceof Metadata) {
                            metadata.addAll((Metadata) object);
                        } else {
                            metadata.add(field, object.toString());
                        }
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
                    final RiakIndex annotation = entry.getKey();
                    final String name = indexName(annotation, property);

                    if (name == null) try {
                        final IndexMap map = property.read(instance, IndexMap.class);
                        if (map != null) indexMap.addAll(map);
                        continue;
                    } catch (IntrospectionException exception) {
                        throw new IllegalStateException("Unnamed @RiakIndex annotation should return an IndexMap for" + property, exception);
                    }

                    for (Object object: combine(property.readAll(instance))) {
                        final IndexType type;
                        switch (annotation.type()) {
                            case AUTODETECT: type = object instanceof Number ? INTEGER : BINARY; break;
                            case INTEGER:    type = INTEGER; break;
                            case BINARY:     type = BINARY;  break;
                            default: throw new IllegalStateException("Unsupported index type " + annotation.type() + " reading " + property);
                        }

                        if (object instanceof IndexMap) {
                            indexMap.addAll((IndexMap) object);
                        } else {
                            indexMap.add(name, type, object.toString());
                        }
                    }
                }
            }
        }

        return indexMap;
    }

    public <T> LinksMap getLinksMap(T instance) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);
        final LinksMap linksMap = new LinksMap(client);

        for (Entry<RiakLink, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakLink.class).entrySet()) {
            final RiakLink annotation = entry.getKey();
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canRead()) {

                    /* Get the link tag */
                    final String tag = linkTag(annotation, property);
                    if ((tag == null) || ("".equals(tag))) try {
                        final LinksMap map = property.read(instance, LinksMap.class);
                        if (map != null) linksMap.addAll(map);
                        continue;
                    } catch (IntrospectionException exception) {
                        throw new IllegalStateException("Unnamed @RiakLink annotation should return a LinksMap for" + property, exception);
                    }

                    /* Get the key and add our link */
                    for (Object link: combine(property.readAll(instance))) {
                        if (link instanceof LinksMap) {
                            linksMap.addAll((LinksMap) link);
                        } else if (link instanceof Key) {
                            linksMap.add(tag, (Key) link);
                        } else if (link instanceof String) {
                            linksMap.add(tag, new Key(client, (String) link));
                        } else {
                            linksMap.add(tag, getKey(link));
                        }
                    }
                }
            }
        }

        return linksMap;
    }

    /* ====================================================================== */

    public <T> RiakIntrospector setKey(T instance, Key key) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);
        final Bucket bucket = client.getBucket(key.getBucketName());

        for (Entry<RiakBucket, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakBucket.class).entrySet()) {
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canWrite(Bucket.class)) property.write(instance, bucket);
                if (property.canWrite(String.class)) property.write(instance, key.getBucketName());
            }
        }

        for (Entry<RiakKey, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakKey.class).entrySet()) {
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canWrite(Key.class)) property.write(instance, key);
                if (property.canWrite(String.class)) property.write(instance, key.getName());
            }
        }

        return this;
    }

    public <T> RiakIntrospector setIndexMap(T instance, IndexMap indexMap) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);

        for (Entry<RiakIndex, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakIndex.class).entrySet()) {
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canWrite()) {
                    final RiakIndex annotation = entry.getKey();
                    final String name = indexName(annotation, property);

                    /* Gets the whole map? Easy! */
                    if (property.canWrite(IndexMap.class)) {
                        property.write(instance, indexMap);
                        continue;
                    } else if (name == null) {
                        throw new IllegalStateException("Unnamed @RiakIndex annotation should accept an IndexMap for " + property);
                    }

                    /* Writes are dependant on type here */
                    switch (annotation.type()) {
                        case INTEGER: for (String value: indexMap.get(name, INTEGER)) property.write(instance, value); break;
                        case BINARY:  for (String value: indexMap.get(name, BINARY))  property.write(instance, value); break;
                        case AUTODETECT:
                            if (property.canWrite(Number.class)) {
                                for (String value: indexMap.get(name, INTEGER)) property.write(instance, value);
                            } else if (property.canWrite(Integer.class)) {
                                for (String value: indexMap.get(name, INTEGER)) property.write(instance, value);
                            } else if (property.canWrite(Long.class)) {
                                for (String value: indexMap.get(name, INTEGER)) property.write(instance, value);
                            } else if (property.canWrite(String.class)) {
                                for (String value: indexMap.get(name, BINARY))  property.write(instance, value);
                            } else {
                                throw new IllegalStateException("Non-typed @RiakIndex property must accept either a Number, Integer, Long or String " + property);
                            }
                            break;
                        default: throw new IllegalStateException("Unsupported index type " + annotation.type() + " reading " + property);
                    }
                }
            }
        }

        return this;
    }


    public <T> RiakIntrospector setLinksMap(T instance, LinksMap linksMap) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);

        for (Entry<RiakLink, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakLink.class).entrySet()) {
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canWrite()) {
                    final RiakLink annotation = entry.getKey();
                    final String tag = linkTag(annotation, property);

                    /* Gets the whole map? Easy! */
                    if (property.canWrite(LinksMap.class)) {
                        property.write(instance, linksMap);
                        continue;
                    } else if (tag == null) {
                        throw new IllegalStateException("Unnamed @RiakLink annotation should accept an LinksMap for " + property);
                    }

                    /* Writes are dependant on type here */
                    for (Key key: linksMap.get(tag)) {
                        if (property.canWrite(Key.class)) property.write(instance, key);
                        else property.write(instance, key.getLocation());
                    }
                }
            }
        }

        return this;
    }

    public <T> RiakIntrospector setMetadata(T instance, Metadata metadata) {
        final IntrospectionDescriptor<T> descriptor = descriptor(instance);

        for (Entry<RiakMetadata, Set<IntrospectedProperty<T>>> entry: descriptor.getProperties(RiakMetadata.class).entrySet()) {
            for (IntrospectedProperty<T> property: entry.getValue()) {
                if (property.canWrite()) {
                    final RiakMetadata annotation = entry.getKey();
                    final String field = metadataField(annotation, property);

                    /* Gets the whole map? Easy! */
                    if (property.canWrite(Metadata.class)) {
                        property.write(instance, metadata);
                        continue;
                    } else if (field == null) {
                        throw new IllegalStateException("Unnamed @RiakMetadata annotation should accept an Metadata for " + property);
                    }

                    /* Do our best... */
                    for (String value: metadata.get(field)) {
                        property.write(instance, value);
                    }
                }
            }
        }

        return this;
    }

}
