/* ========================================================================== *
w * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
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

import java.lang.reflect.Constructor;

import javax.inject.Singleton;

import org.usrz.libs.inject.InjectionException;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.inject.TypeLiteral;
import org.usrz.libs.inject.bind.Resolver;
import org.usrz.libs.inject.deps.InstanceCreator;
import org.usrz.libs.inject.utils.Annotations;
import org.usrz.libs.inject.utils.Constructors;

/**
 * A {@link Resolver} creating instances of a {@link TypeLiteral} using a
 * {@link InstanceCreator}.
 * <p>
 * This instance will manage {@link Singleton} annotated objects creation.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class InjectingResolver<T> implements Resolver<T> {

    private final TypeLiteral<T> type;
    private final InstanceCreator<T> constructor;
    private final boolean singleton;
    private volatile T instance;

    /**
     * Create an {@link InjectingResolver} from the specified
     * {@link TypeLiteral}, detecting its injectable constructor.
     *
     * @see Constructors#getConstructor(Class)
     */
    public InjectingResolver(TypeLiteral<T> type) {
        final Constructor<? extends T> constructor = Constructors.getConstructor(notNull(type, "Null type").getRawClass());
        this.constructor = new InstanceCreator<>(type, notNull(constructor, "Null constructor"));
        this.singleton = Annotations.isSingleton(type.getRawClass());
        this.type = type;
    }

    @Override
    public T resolve(Injector injector)
    throws InjectionException {
        if (singleton) {
            if (instance != null) return instance;
            synchronized (this) {
                if (instance != null) return instance;
                instance = injector.injectMembers(constructor.resolve(injector), type);
            }
            return instance;

        } else {
            return injector.injectMembers(constructor.resolve(injector), type);
        }
    }
}
