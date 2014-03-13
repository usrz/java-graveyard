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
package org.usrz.libs.riak.introspection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.usrz.libs.riak.JsonTest.RiakAnnotationIntrospector;
import org.usrz.libs.riak.Key;
import org.usrz.libs.riak.introspection.converters.ListConverter;
import org.usrz.libs.riak.introspection.converters.StringKeyConverter;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class RiakIntrospector {

    private final BasicClassIntrospector classIntrospector = new BasicClassIntrospector();
    private final RiakAnnotationIntrospector introspector = new RiakAnnotationIntrospector();

    private final SerializationConfig serializationConfig;
    private final DeserializationConfig deserializationConfig;
    private final TypeFactory typeFactory;

    public RiakIntrospector() {
        final ObjectMapper mapper = new ObjectMapper();
        typeFactory = mapper.getTypeFactory();
        serializationConfig = mapper.getSerializationConfig().with(introspector);
        deserializationConfig = mapper.getDeserializationConfig().with(introspector);

        System.err.println(typeFactory.constructArrayType(String.class));
        System.err.println(typeFactory.constructType(int.class).containedTypeCount());
        System.err.println(typeFactory.constructType(Integer.class).containedTypeCount());
        System.err.println(new ListConverter<String, Key>(null, new StringKeyConverter(null)).getInputType(typeFactory).containedTypeCount());
        System.err.println(new ListConverter<String, Key>(null, new StringKeyConverter(null)).getOutputType(typeFactory));
    }

    public BeanDescription getEntityReader(Class<?> entityClass) {
        if (entityClass == null) return null;
        final JavaType type = typeFactory.constructType(entityClass);
        return classIntrospector.forSerialization(serializationConfig, type, serializationConfig);
    }

    public BeanDescription getEntityReader(Object object) {
        if (object == null) return null;
        return getEntityReader(object instanceof Class ? (Class<?>) object : object.getClass());
    }

    public BeanDescription getEntityWriter(Class<?> entityClass) {
        if (entityClass == null) return null;
        final JavaType type = typeFactory.constructType(entityClass);
        return classIntrospector.forDeserialization(deserializationConfig, type, deserializationConfig);
    }

    public BeanDescription getEntityWriter(Object object) {
        if (object == null) return null;
        return getEntityWriter(object instanceof Class ? (Class<?>) object : object.getClass());
    }

    private final Map<Class<?>, Class<?>> converters = new HashMap<>();

    public boolean getConverter(Class<?> in, Class<?> out) {
        converters.put(Iterable.class, HashSet.class);


//        if (in.containedTypeCount() != out.containedTypeCount()) return false;

        for (Map.Entry<Class<?>, Class<?>> entry: converters.entrySet()) {
            final Class<?> cin = entry.getKey();
            final Class<?> cout = entry.getValue();

            if ((cin.isAssignableFrom(in)) &&
                (out.isAssignableFrom(cout))) {
                return true;
            }
        }

        return false;
    }

}
