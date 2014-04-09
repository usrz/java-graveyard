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

import static org.usrz.libs.configurations.Configurations.EMPTY_CONFIGURATIONS;
import static org.usrz.libs.inject.utils.Annotations.findProvidedBy;
import static org.usrz.libs.inject.utils.Parameters.notNull;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;
import javax.inject.Provider;

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.Key;
import org.usrz.libs.inject.ProvisionException;
import org.usrz.libs.inject.TypeLiteral;
import org.usrz.libs.inject.bind.Binding;
import org.usrz.libs.inject.bind.Bindings;
import org.usrz.libs.inject.bind.Resolver;
import org.usrz.libs.inject.bind.Scoping;
import org.usrz.libs.inject.deps.TypeInjector;
import org.usrz.libs.inject.utils.ResolutionStack;

/**
 * An implementation of the {@link Injector} interface.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class InjectorImpl implements Injector {

    private static final Key<Injector> INJECTOR_KEY = Key.of(Injector.class);
    private static final Key<Configurations> CONFIGURATIONS_KEY = Key.of(Configurations.class);

    private final ResolutionStack stack = new ResolutionStack();

    private final Configurations configurations;
    private final boolean staticInjection;
    private final InjectorImpl parent;
    private final Bindings bindings;

    public InjectorImpl(boolean supportStaticInjection, Bindings bindings) {
        this(null, supportStaticInjection, bindings);
    }

    private InjectorImpl(InjectorImpl parent, boolean supportStaticInjection, Bindings bindings) {
        this.parent = parent;
        this.bindings = notNull(bindings, "Null bindings");
        staticInjection = supportStaticInjection;

        /* Initialize configurations */
        final Binding<Configurations> config = bindings.getBinding(CONFIGURATIONS_KEY);
        configurations = config == null ? EMPTY_CONFIGURATIONS : getInstance(config.getKey());

        /* Eager injection */
        bindings.forEach((b) -> { if (b.isEagerlyInjected()) getInstance(b.getKey()); });
    }

    /* ====================================================================== */

    private final WeakHashMap<Scoping, Injector> scopedInjectors = new WeakHashMap<>();

    @Override
    public Injector createChild(Bindings bindings) {
        final Scoping scoping = bindings.getScoping();
        if (scoping == null) return new InjectorImpl(this, staticInjection, bindings);

        synchronized (scopedInjectors) {
            Injector scoped = scopedInjectors.get(scoping);
            if (scoped != null) return scoped;

            scoped = new InjectorImpl(this, staticInjection, bindings);
            scopedInjectors.put(scoping, scoped);
            return scoped;
        }
    }

    /* ====================================================================== */

    @Override
    public boolean isBound(Key<?> key) {
        notNull(key, "Null key");

        /* Injector and providers always return true */
        if (INJECTOR_KEY.equals(key)) return true;
        if (key.isProviderKey()) return true;

        /* Bindings are checked (normal and dynamic) */
        if (bindings.getBinding(key) != null) return true;
        if (dynamicBindings.contains(key)) return true;

        /* Configuration value? */
        if (Named.class.equals(key.getAnnotationType())) {
            if (configurations.containsKey(((Named) key.getAnnotation()).value()) && (
                    (key.isTypeOf(Boolean.class)) || (key.isTypeOf(Byte.class))    ||
                    (key.isTypeOf(Double.class))  || (key.isTypeOf(Float.class))   ||
                    (key.isTypeOf(Integer.class)) || (key.isTypeOf(Long.class))    ||
                    (key.isTypeOf(Short.class))   || (key.isTypeOf(File.class))    ||
                    (key.isTypeOf(String.class))  || (key.isTypeOf(URI.class))     ||
                    (key.isTypeOf(URL.class)))) {
                return true;
            }
        }

        /* Check the parent, if we have one */
        return parent != null ? parent.isBound(key) : false;
    }

    @Override
    public Set<Key<?>> getBoundKeys(boolean includeDynamic, boolean includeParent) {
        final Set<Key<?>> keys = new HashSet<>();

        /* Parent bindings first */
        if ((includeParent) && (parent != null))
            keys.addAll(parent.getBoundKeys(includeDynamic, true));

        /* Local "static" bindings */
        bindings.forEach((binding) -> keys.add(binding.getKey()));

        /* Local "dynamic" bindings */
        if (includeDynamic) keys.addAll(dynamicBindings.keySet());

        /* Done! */
        return keys;
    }


    @Override
    public <T> T getInstance(Key<T> key) {
        notNull(key, "Null key");

        /* If someone is asking for an Injector, return this */
        if (key.equals(INJECTOR_KEY)) return key.cast(this);

        /* If someone is looking for a Provider, see if we can return one */
        if (key.isProviderKey()) return InjectorProvider.create(this, key);

        /* Local binding? */
        final Binding<T> binding = stack.enter(bindings.getBinding(key));
        try {
            if (binding != null) return binding.resolve(this);
        } catch (ProvisionException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ProvisionException("Unable to provide value for binding", key, exception, stack.getBindings());
        } finally {
            stack.exit(binding);
        }

        /* Configuration value? */
        if (Named.class.equals(key.getAnnotationType())) {
            final String name = ((Named) key.getAnnotation()).value();
            if (configurations.containsKey(name)) {
                if (key.isTypeOf(Boolean.class)) return key.cast(configurations.getBoolean(name));
                if (key.isTypeOf(Byte.class))    return key.cast(configurations.getByte(name));
                if (key.isTypeOf(Double.class))  return key.cast(configurations.getDouble(name));
                if (key.isTypeOf(Float.class))   return key.cast(configurations.getFloat(name));
                if (key.isTypeOf(Integer.class)) return key.cast(configurations.getInteger(name));
                if (key.isTypeOf(Long.class))    return key.cast(configurations.getLong(name));
                if (key.isTypeOf(Short.class))   return key.cast(configurations.getShort(name));
                if (key.isTypeOf(File.class))    return key.cast(configurations.getFile(name));
                if (key.isTypeOf(String.class))  return key.cast(configurations.getString(name));
                if (key.isTypeOf(URI.class))     return key.cast(configurations.getURI(name));
                if (key.isTypeOf(URL.class))     return key.cast(configurations.getURL(name));
            }
        }

        /* Parent binding? */
        if (parent != null) return parent.getInstance(key);

        /* Dynamic binding attempt only on root injector */
        final Binding<T> dynamic = stack.enter(getDynamicBinding(key));
        try {
            return dynamic.resolve(this);
        } catch (ProvisionException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ProvisionException("Unable to provide value for dynamic binding", key, exception, stack.getBindings());
        } finally {
            stack.exit(dynamic);
        }
    }

    /* ---------------------------------------------------------------------- */

    private final ConcurrentHashMap<Key<?>, Binding<?>> dynamicBindings = new ConcurrentHashMap<>();

    private <T> Binding<T> getDynamicBinding(Key<T> key) {
        @SuppressWarnings("unchecked")
        Binding<T> binding = (Binding<T>) dynamicBindings.get(key);
        if (binding != null) return binding;

        synchronized (dynamicBindings) {
            try {
                final TypeLiteral<T> type = key.getTypeLiteral();

                /* Handle @ProvidedBy dynamic bindings */
                final TypeLiteral<? extends Provider<T>> providerType = findProvidedBy(type.getRawClass());
                final Resolver<T> resolver = providerType == null ?
                                new InjectingResolver<>(type) :
                                new ProviderResolver<>(new InjectingResolver<>(providerType));

                dynamicBindings.put(key, binding = new Binding<T>(key, resolver, false, "(dynamic binding)"));
            } catch (Exception exception) {
                throw new ProvisionException("Unable to create dynamic binding", key, exception, stack.getBindings());
            }
        }
        return binding;
    }

    /* ====================================================================== */

    @Override
    public <T> T injectMembers(T instance, TypeLiteral<T> type)
    throws InjectionException {
        /* Simply get the type injector and return the injected instance */
        getTypeInjector(type).inject(this, instance);
        return instance;
    }

    /* ---------------------------------------------------------------------- */

    private final ConcurrentHashMap<TypeLiteral<?>, TypeInjector<?>> typeInjectors = new ConcurrentHashMap<>();

    private <T> TypeInjector<T> getTypeInjector(TypeLiteral<T> type)
    throws InjectionException {

        /* Always delegate to the parent */
        if (parent != null) return parent.getTypeInjector(type);

        /* Quickly check our descriptors cache */
        @SuppressWarnings("unchecked")
        TypeInjector<T> descriptor = (TypeInjector<T>) typeInjectors.get(type);
        if (descriptor != null) return descriptor;

        /* Static injection requires synchronization */
        if (staticInjection) synchronized (typeInjectors) {
            new TypeInjector<T>(type, true).inject(this, type.getRawClass());
        }

        /* Now create they instance type descriptor */
        descriptor = new TypeInjector<T>(type, false);

        /* Remember this descriptor if another thread didn't beat us to it */
        typeInjectors.putIfAbsent(type, descriptor);
        return descriptor;
    }
}
