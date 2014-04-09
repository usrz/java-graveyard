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
package org.usrz.libs.inject.impl;

import static org.usrz.libs.inject.utils.Parameters.notNull;

import java.lang.annotation.Annotation;

import org.usrz.libs.inject.Annotator;
import org.usrz.libs.inject.Binder;
import org.usrz.libs.inject.Exposer;
import org.usrz.libs.inject.Initializer;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.Key;
import org.usrz.libs.inject.TypeLiteral;
import org.usrz.libs.inject.bind.Resolver;

/**
 * A {@link IsolateResolver} links resolution of
 * {@linkplain Binder#expose(Key) exposed} keys from child {@link Binder}s
 * to their parents.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class IsolateResolver<T> implements Resolver<T>, Exposer {

    private Key<T> key;
    private final Binder binder;
    private final Annotator<T> annotator;

    private volatile Injector child;

    /**
     * Create a new {@link IsolateResolver} associated with the given
     * {@link TypeLiteral}, bound to the specific parent {@link Annotator}
     * and child {@link Binder}.
     */
    public IsolateResolver(TypeLiteral<T> type, Annotator<T> parent, Binder binder) {
        this.annotator = notNull(parent, "Null parent annotator");
        this.key = Key.of(notNull(type, "Null type"));
        this.binder = notNull(binder, "Null binder");
    }

    @Override
    public Initializer with(Annotation annotation) {
        this.key = key.with(annotation);
        return annotator.with(annotation);
    }

    @Override
    public Initializer with(Class<? extends Annotation> annotationType) {
        this.key = key.with(annotationType);
        return annotator.with(annotationType);
    }

    @Override
    public void withEagerInjection() {
        annotator.withEagerInjection();
    }

    @Override
    public void asSingleton() {
        annotator.asSingleton();
    }

    @Override
    public T resolve(Injector injector) {
        if (child != null) return child.getInstance(key);
        synchronized(this) {
            if (child != null) return child.getInstance(key);
            child = injector.createChild(binder.getBindings());
        }
        return child.getInstance(key);
    }

}
