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
package org.usrz.libs.inject;

import javax.inject.Inject;
import javax.inject.Provider;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class EagerInjectionTest extends AbstractTest {

    @Test
    public void testEagerType() {
        Injector.create((binder) -> binder.bind(EagerType.class).withEagerInjection());
        assertTrue(EagerType.injected, "Eager Type not injected");
    }

    @Test
    public void testEagerProvider() {
        Injector.create((binder) -> binder.bind(EagerProvided.class).toProvider(EagerProvider.class).withEagerInjection());
        assertTrue(EagerProvider.injected, "Eager Provider not injected");
        assertTrue(EagerProvided.injected, "Eager Provided not injected");
    }

    public static class EagerType {
        private static boolean injected = false;
        @Inject private EagerType() { injected = true; }
    }

    public static class EagerProvided {
        private static boolean injected = false;
        private EagerProvided() { injected = true; }
    }

    public static class EagerProvider implements Provider<EagerProvided> {
        private static boolean injected = false;
        @Inject private EagerProvider() { injected = true; }
        @Override public EagerProvided get() { return new EagerProvided(); }
    }
}
