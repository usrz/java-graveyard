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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;
import org.usrz.libs.riak.annotations.RiakKey;
import org.usrz.libs.riak.introspection.RiakIntrospector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class JsonTest {

    @Test
    public void jsonTest() throws Exception {
        RiakIntrospector introspector = new RiakIntrospector();

        System.err.println(introspector.getConverter(Set.class, Set.class));

        final Bean bean = new Bean();

        System.err.println("========================= SER");

        final BeanDescription serializationDescription = introspector.getEntityReader(bean);
        final List<BeanPropertyDefinition> serializationProperties = serializationDescription.findProperties();
        for (BeanPropertyDefinition property: serializationProperties) {
            System.err.println(property);
            property.couldDeserialize();
            System.err.println(" ----> " + property.getAccessor().getClass());
            System.err.println(" ----> " + property.getAccessor().getRawType());
        }

        System.err.println("========================= DESER");

        final BeanDescription deserializationDescription = introspector.getEntityWriter(bean);
        final List<BeanPropertyDefinition> deserializationProperties = deserializationDescription.findProperties();
        for (BeanPropertyDefinition property: deserializationProperties) {
            System.err.println(property);
            System.err.println(" ----> " + property.getMutator().getClass());
            System.err.println(" ----> " + property.getMutator().getRawType());
        }


//
//        final ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//
//        final String json = mapper.writeValueAsString(new Bean());
//        System.err.println(json);
//        mapper.readValue(json, Bean.class);

    }

//    @JsonIdentityInfo(generator=PropertyGenerator.class)
    public static class Bean {

        @RiakKey
        private Key key = new Key("KeyKey");
        private final String value = "defaultValue";
        private final Bean2 bean = new Bean2();

        @RiakKey
        public Key getKey() {
            return key;
        }

        @RiakKey
        public void setKey(Key key) {
            this.key = key;
        }

//                public String getValue() {
//            return value;
//        }
//
//        public void setValue(String value) {
//            this.value = value;
//        }
//
//        //@JsonIdentityReference(alwaysAsId=true)
//        public Bean2 getBean() {
//            return bean;
//        }
//
//        //@JsonIdentityReference(alwaysAsId=true)
//        public void setBean(Bean2 bean) {
//            this.bean = bean;
//        }

    }

//    @JsonIdentityInfo(generator=PropertyGenerator.class)
    public static class Bean2 {

        private Key key = new Key("NestedObjectKey");
        private String value = "nestedValue";

        @JsonProperty("@id")
        public Key getKey() {
            return key;
        }

        @JsonProperty("@id")
        public void setKey(Key key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Key {
        private final String key;
        public Key(@JsonProperty("key") String key) { this.key = key; }
        public String getKey() { return key; }
        @Override
        public String toString() { return "(Key[" + key + "])"; }
    }

    public static class RiakAnnotationIntrospector extends NopAnnotationIntrospector {

        @Override
        public PropertyName findNameForSerialization(Annotated a) {
            System.err.println("FindNameForSerialization " + a);
            if (a.hasAnnotation(RiakKey.class)) {
                return new PropertyName("@key");
            }

            return null;
//            throw new IllegalArgumentException("Should be ignored! " + a);
        }

        @Override
        public PropertyName findNameForDeserialization(Annotated a) {
            System.err.println("FindNameForDeserialization " + a);
            if (a.hasAnnotation(RiakKey.class)) {
                return new PropertyName("@key");
            }

            return null;
//            throw new IllegalArgumentException("Should be ignored! " + a);
        }

//        @Override
//        public boolean hasAnyGetterAnnotation(AnnotatedMethod a) {
//            new Exception().printStackTrace();
//            System.err.print("Checking getter " + a);
//            if (a.hasAnnotation(RiakKey.class)) { System.err.println(" ==> YES"); return true; }
//            System.err.println(" ==> NO ");
//            return false;
//        }
//
//        @Override
//        public boolean hasAnySetterAnnotation(AnnotatedMethod a) {
//            System.err.print("Checking setter " + a);
//            if (a.hasAnnotation(RiakKey.class)) { System.err.println(" ==> YES"); return true; }
//            System.err.println(" ==> NO ");
//            return false;
//        }

        @Override
        public String findSerializationName(AnnotatedMethod a) {
            System.err.println("FindSerializationName " + a);
            if (a.hasAnnotation(RiakKey.class)) {
                return "@key";
            }

            return null;

        }

        @Override
        public String findDeserializationName(AnnotatedMethod a) {
            System.err.println("FindDeserializationName " + a);
            if (a.hasAnnotation(RiakKey.class)) {
                return "@key";
            }

            return null;

        }

        @Override
        public Object findDeserializationConverter(Annotated a) {
            System.err.println("FindDeserializationConverter " + a);
            return super.findDeserializationConverter(a);
        }

        @Override
        public Object findSerializationConverter(Annotated a) {
            System.err.println("FindSerializationConverter " + a);
            return super.findSerializationConverter(a);
        }

        @Override
        public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config,
                AnnotatedClass ac,
                JavaType baseType) {
            System.err.println("FindTypeResolver " + ac);
            return null;
        }

//        @Override
//        public boolean hasAsValueAnnotation(AnnotatedMethod a) {
//            new Exception().printStackTrace();
//            System.err.print("Checking value " + a);
//            if (a.hasAnnotation(RiakKey.class)) { System.err.println(" ==> YES"); return true; }
//            System.err.println(" ==> NO ");
//            return false;
//        }

//        @Override
//        public Object findSerializer(Annotated a) {
//            if (a.getRawType().equals(Key.class)) return RiakKeySerializer.class;
//            return null;
//        }


        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            if (m.hasAnnotation(RiakKey.class)) return false;
            else return true;
        }

    }


    public static class RiakKeySerializer extends StdSerializer<Key> {

        protected RiakKeySerializer() {
            super(Key.class);
        }

        @Override
        public void serialize(Key value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonGenerationException {
            throw new UnsupportedOperationException();
        }

    }
}
