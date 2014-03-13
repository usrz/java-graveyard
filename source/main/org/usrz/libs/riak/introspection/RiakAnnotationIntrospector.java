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

import static com.fasterxml.jackson.databind.util.BeanUtil.okNameForGetter;

import java.lang.reflect.Type;

import org.usrz.libs.riak.IndexType;
import org.usrz.libs.riak.annotations.RiakIndex;
import org.usrz.libs.riak.annotations.RiakKey;
import org.usrz.libs.riak.annotations.RiakLink;
import org.usrz.libs.riak.annotations.RiakMetadata;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

public class RiakAnnotationIntrospector extends NopAnnotationIntrospector {

    private static final String NAMESPACE_LINKS =    "@riak_links";
    private static final String NAMESPACE_INDEXES =  "@riak_indexes";
    private static final String NAMESPACE_METADATA = "@riak_metadata";

    public static final PropertyName NAME_KEY =      new PropertyName("@riak_key");

    public static final PropertyName NAME_LINKS_BASE =    new PropertyName("", NAMESPACE_LINKS);
    public static final PropertyName NAME_INDEXES_BASE =  new PropertyName("", NAMESPACE_INDEXES);
    public static final PropertyName NAME_METADATA_BASE = new PropertyName("", NAMESPACE_METADATA);

    /* ====================================================================== */

    private PropertyName propertyNameForIndex(RiakIndex annotation, Type genericType, String name) {

        /* No name? Ignore all the rest */
        if (name == null) return NAME_INDEXES_BASE;

        /* Check type */
        IndexType type = annotation.type().getIndexType();

        /* Detect type by name */
        if (type == null) {
            for (IndexType currentType: IndexType.values()) {
                if (name.endsWith(currentType.getSuffix())) {
                    type = currentType;
                    break;
                }
            }
        }

        /* Detect type by argument */
        if (type == null) type = IndexType.typeFor(genericType);

        /* Return either base name or properly named property */
        return NAME_INDEXES_BASE.withSimpleName(type == null ? null : name + type.getSuffix());

    }

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        System.err.println("FindNameForSerialization " + a);

        if (a.hasAnnotation(RiakKey.class)) return NAME_KEY;

        if (a.hasAnnotation(RiakLink.class)) {
            final String tag = a.getAnnotation(RiakLink.class).value();
            return NAME_LINKS_BASE.withSimpleName(
                       (!tag.isEmpty())               ? tag :
                       (a instanceof AnnotatedField)  ? a.getName() :
                       (a instanceof AnnotatedMethod) ? okNameForGetter((AnnotatedMethod) a) :
                       null);
        }

        if (a.hasAnnotation(RiakMetadata.class)) {
            final String field = a.getAnnotation(RiakMetadata.class).value();
            return NAME_METADATA_BASE.withSimpleName(
                       (!field.isEmpty())             ? field :
                       (a instanceof AnnotatedField)  ? a.getName() :
                       (a instanceof AnnotatedMethod) ? okNameForGetter((AnnotatedMethod) a) :
                       null);
        }

        if (a.hasAnnotation(RiakIndex.class)) {
            final RiakIndex annotation = a.getAnnotation(RiakIndex.class);
            return propertyNameForIndex(annotation, a.getGenericType(),
                        (!annotation.name().isEmpty())  ? annotation.name():
                        (!annotation.value().isEmpty()) ? annotation.value():
                        (a instanceof AnnotatedField)   ? a.getName() :
                        (a instanceof AnnotatedMethod)  ? okNameForGetter((AnnotatedMethod) a) :
                        null);
        }

        return null;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        System.err.println("FindNameForDeserialization " + a);

        if (a.hasAnnotation(RiakKey.class)) return NAME_KEY;

        if (a.hasAnnotation(RiakLink.class)) {
            final String tag = a.getAnnotation(RiakLink.class).value();
            return NAME_LINKS_BASE.withSimpleName(
                       (!tag.isEmpty())               ? tag :
                       (a instanceof AnnotatedField)  ? a.getName() :
                       (a instanceof AnnotatedMethod) ? okNameForGetter((AnnotatedMethod) a) :
                       null);
        }

        if (a.hasAnnotation(RiakMetadata.class)) {
            final String field = a.getAnnotation(RiakMetadata.class).value();
            return NAME_METADATA_BASE.withSimpleName(
                       (!field.isEmpty())             ? field :
                       (a instanceof AnnotatedField)  ? a.getName() :
                       (a instanceof AnnotatedMethod) ? okNameForGetter((AnnotatedMethod) a) :
                       null);
        }

        if (a.hasAnnotation(RiakIndex.class)) {
            final RiakIndex annotation = a.getAnnotation(RiakIndex.class);

            final Type genericType = a instanceof AnnotatedField ?
                                             a.getGenericType() :
                                     a instanceof AnnotatedMethod ?
                                             ((AnnotatedMethod) a).getGenericParameterType(0) :
                                     null;

            return propertyNameForIndex(annotation, genericType,
                        (!annotation.name().isEmpty())  ? annotation.name():
                        (!annotation.value().isEmpty()) ? annotation.value():
                        (a instanceof AnnotatedField)   ? a.getName() :
                        (a instanceof AnnotatedMethod)  ? okNameForGetter((AnnotatedMethod) a) :
                        null);
        }

        return null;
    }


    @Override
    public String findSerializationName(AnnotatedMethod a) {
        return findNameForSerialization(a).getSimpleName();
    }

    @Override
    public String findDeserializationName(AnnotatedMethod a) {
        return findNameForDeserialization(a).getSimpleName();
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
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        if (m.hasAnnotation(RiakKey.class)) return false;
        if (m.hasAnnotation(RiakLink.class)) return false;
        if (m.hasAnnotation(RiakIndex.class)) return false;
        if (m.hasAnnotation(RiakMetadata.class)) return false;
        else return true;
    }

}
