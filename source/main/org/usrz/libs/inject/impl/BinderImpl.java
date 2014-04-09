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

import static org.usrz.libs.inject.utils.Annotations.findProvidedBy;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import org.usrz.libs.inject.Annotator;
import org.usrz.libs.inject.Binder;
import org.usrz.libs.inject.Exposer;
import org.usrz.libs.inject.Initializer;
import org.usrz.libs.inject.Key;
import org.usrz.libs.inject.Linker;
import org.usrz.libs.inject.TypeLiteral;
import org.usrz.libs.inject.bind.Binding;
import org.usrz.libs.inject.bind.Bindings;
import org.usrz.libs.inject.bind.Resolver;
import org.usrz.libs.inject.bind.Scoping;

/**
 * An implementation of the {@link Binder} interface.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class BinderImpl implements Binder {

    private final List<Builder<?>> bindings = new ArrayList<>();
    private final List<BinderImpl> children = new ArrayList<>();
    private final Set<String> ignoredSources = new HashSet<>();
    private final Scoping scoping = new Scoping();
    private final BinderImpl parent ;

    public BinderImpl() {
        this(null);
    }

    private BinderImpl(BinderImpl parent) {
        ignoreSources(BinderImpl.class, Builder.class);
        this.parent = parent;
    }

    @Override
    public <T> Annotator<T> bind(TypeLiteral<T> type) {
        final Builder<T> builder = new Builder<>(type);
        bindings.add(builder);
        return builder;
    }

    @Override
    public BinderImpl isolate() {
        final BinderImpl child = new BinderImpl(this);
        children.add(child);
        return child;
    }

    @Override
    public <T> Exposer expose(TypeLiteral<T> type) {
        if (parent == null) return new Exposer() {
            @Override public Initializer with(Annotation annotation) { return this; }
            @Override public Initializer with(Class<? extends Annotation> annotationType) { return this; }
            @Override public void withEagerInjection() { }
            @Override public void asSingleton() { }
        };

        final Builder<T> builder = new Builder<>(type);
        final IsolateResolver<T> resolver = new IsolateResolver<>(type, builder, this);
        builder.resolver = resolver;
        parent.bindings.add(builder);
        return resolver;
    }

    /* ====================================================================== */

    @Override
    public Bindings getBindings() {
        final Iterator<Builder<?>> iterator = bindings.iterator();
        return new Bindings(scoping, () ->
            new Iterator<Binding<?>>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Binding<?> next() {
                    return iterator.next().getBinding();
                }

            });
    }


    /* ====================================================================== */

    @Override
    public void ignoreSources(Class<?>... sources) {
        for (Class<?> source: sources) ignoreSource(source);
    }

    private void ignoreSource(Class<?> source) {
        if (source == null) return;
        ignoredSources.add(source.getName());
        for (Class<?> i: source.getInterfaces()) ignoreSource(i);
        ignoreSource(source.getSuperclass());
    }

    private String getSource() {
        for (StackTraceElement element: new Throwable().getStackTrace()) {
            if (ignoredSources.contains(element.getClassName())) continue;
            return element.toString();
        }
        return null;
    }

    /* ====================================================================== */

    private class Builder<T> implements Annotator<T>, Linker<T> {

        private final TypeLiteral<T> type;
        private Resolver<? extends T> resolver;
        private boolean eagerInjection;
        private boolean singletonInjection;
        private final String source;
        private Key<T> key;

        private Builder(TypeLiteral<T> type) {
            this.key = Key.of(type);
            this.source = getSource();
            this.type = type;

            /* See if this is annotated with @ProvidedBy */
            final Class<T> raw = type.getRawClass();
            final TypeLiteral<? extends Provider<T>> provider = findProvidedBy(raw);
            if (provider != null) this.toProvider(provider);
        }

        private Binding<T> getBinding() {
            if (resolver == null) resolver = new InjectingResolver<>(type);
            if (singletonInjection) resolver = new SingletonResolver<>(resolver);
            return new Binding<>(key, resolver, eagerInjection, source);
        }

        /* ------------------------------------------------------------------ */

        @Override
        public Linker<T> with(Annotation annotation) {
            key = key.with(annotation);
            return this;
        }

        @Override
        public Linker<T> with(Class<? extends Annotation> annotationType) {
            key = key.with(annotationType);
            return this;
        }

        /* ------------------------------------------------------------------ */

        @Override
        public Initializer toInstance(T instance) {
            this.resolver = new InstanceResolver<>(instance);
            return this;
        }

        @Override
        public <X extends T, P extends Provider<X>> Initializer toProvider(P provider) {
            final Resolver<P> resolver = new InstanceResolver<>(provider);
            this.resolver = new ProviderResolver<X>(resolver);
            return this;
        }

        @Override
        public <X extends T, P extends Provider<X>> Initializer toProvider(TypeLiteral<P> provider) {
            final Resolver<P> resolver = new InjectingResolver<>(provider);
            this.resolver = new ProviderResolver<X>(resolver);
            return this;
        }

        @Override
        public Initializer to(TypeLiteral<? extends T> type) {
            this.resolver = new InjectingResolver<>(type);
            return this;
        }

        /* ------------------------------------------------------------------ */

        @Override
        public void withEagerInjection() {
            eagerInjection = true;
        }

        @Override
        public void asSingleton() {
            singletonInjection = true;
        }

    }
}
