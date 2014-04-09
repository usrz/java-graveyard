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
package org.usrz.libs.inject.deps;

import static org.usrz.libs.inject.utils.Signatures.signature;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.TypeLiteral;
import org.usrz.libs.inject.utils.Annotations;

/**
 * A {@link Descriptor} and {@link MemberInjector} for {@link Class}es.
 * <p>
 * This object will delegate the actual injection to a set of known
 * {@link MethodInjector}s and {@link FieldInjector}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class TypeInjector<T>
extends AbstractDescriptor<T>
implements MemberInjector<T> {

    /* This needs to be ordered, fields first, sub-type to super-type */
    private final List<MemberInjector<T>> members;

    /**
     * Create a new {@link TypeInjector} given the specified
     * {@link TypeLiteral}.
     *
     * @param staticInjection If <b>true</b> only static methods and fields
     *                        will be resolved and injected, if <b>false</b>
     *                        only its instance methods and fields will.
     */
    public TypeInjector(TypeLiteral<T> type, boolean staticInjection) {
        super(type);

        final List<MemberInjector<T>> members = new ArrayList<>();
        this.findDescriptors(type, type.getRawClass(), members, new HashMap<>(), staticInjection);
        this.members = Collections.unmodifiableList(members);
    }

    @Override
    public void inject(Injector injector, Object object)
    throws InjectionException {
        for (MemberInjector<T> member: members) {
            member.inject(injector, object);
        }
    }

    @Override
    public List<Dependency<?>> getDependencies() {
        final List<Dependency<?>> dependencies = new ArrayList<>();
        for (MemberInjector<T> member: members)
            dependencies.addAll(member.getDependencies());
        return Collections.unmodifiableList(dependencies);
    }

    /* ====================================================================== */

    private void findDescriptors(TypeLiteral<T> type,                    // the original type literal
                                 Class<?> clazz,                         // the class we're looking at now
                                 List<MemberInjector<T>> members,        // descriptors, in order
                                 Map<String, MethodInjector<T>> methods, // cache of methods, to calculate what's being overridden
                                 boolean staticInjection) {              // flag indicating what kind (static/non-static) to process

        if (clazz == null) return;
        if (clazz == Object.class) return;

        /* First go to the super class (from Object to T, in order) */
        findDescriptors(type, clazz.getSuperclass(), members, methods, staticInjection);

        /* Process all declared fields, @Inject ones are retained */
        for (Field field: clazz.getDeclaredFields()) {
            if (Annotations.isInjectable(field)) {

                /* Skip instance/static fields if not in this phase */
                final int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) != staticInjection) continue;

                /* Add the field to our descriptor */
                members.add(new FieldInjector<>(type, field));
            }
        }

        /* Process all declared methods */
        for (Method method: clazz.getDeclaredMethods()) {

            /* Skip instance/static methods if not in this phase */
            final int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers) != staticInjection) continue;

            /*
             * The signature here is used to override methods. Private method
             * are always prepended by the class name (as they can not be
             * overridden, ever).
             *
             * Public and protected use simple signatures,
             * not prefixed, as they can be overridden by sub classes.
             *
             * Package protected methods are prefixed by package name, as they
             * can be overridden by classes in the same package.
             */
            final String methodSignature = signature(method);
            final String signature =
                    Modifier.isStatic(modifiers)    ? clazz.getName() + '#' + methodSignature :
                    Modifier.isPrivate(modifiers)   ? clazz.getName() + '#' + methodSignature :
                    Modifier.isProtected(modifiers) ? methodSignature :
                    Modifier.isPublic(modifiers)    ? methodSignature :
                        clazz.getPackage().getName() + '#' + methodSignature;

            /*
             * Remove any previous method associated with this signature,
             * basically if a non-@Inject method overrides an @Inject one,
             * we do NOT want to call it!
             */
            final MethodInjector<T> removed = methods.remove(signature);
            if (removed != null) {
                members.remove(removed);
            }

            /* Now that we've cleared any previous association, add this */
            if (Annotations.isInjectable(method)) {
                final MethodInjector<T> descriptor = new MethodInjector<>(type, method);
                methods.put(signature, descriptor);
                members.add(descriptor);
            }
        }
    }
}
