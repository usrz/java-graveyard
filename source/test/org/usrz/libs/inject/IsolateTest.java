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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class IsolateTest extends AbstractTest {

    private static final Map<String, String> map = new HashMap<>();

    @Test
    public void testIsolate() {
        final Set<String> set = new HashSet<>();

        final Injector injector = Injector.create((binder) -> {
            binder.bind(new TypeLiteral<Set<String>>(){}).toInstance(set);
            binder.isolate((binder2) -> {
                binder2.bind(new TypeLiteral<Map<String, String>>(){}).toInstance(map);
                binder2.bind(TheSingleton.class);
                binder2.bind(TheInstance.class);
                binder2.expose(TheInstance.class);
            });
        });

        try {
            assertNotNull(injector.getInstance(TheSingleton.class));
            fail("Actually got an instance of TheSingleton");
        } catch (ProvisionException exception) {
            /* All is good! */
        }

        final TheInstance instance1 = injector.getInstance(TheInstance.class);
        final TheInstance instance2 = injector.getInstance(TheInstance.class);
        assertNotSame(instance1, instance2, "Non-singleton instances are same");
        assertSame(instance1.singleton, instance2.singleton, "Singleton instances are not the same");
        assertSame(instance1.singleton.set, set);
        assertSame(instance2.singleton.set, set);
    }

    @Singleton
    public static class TheSingleton {

        private final Set<String> set;

        @Inject
        private TheSingleton(Set<String> set, Map<String, String> map) {
            Assert.assertSame(map, IsolateTest.map);
            this.set = set;
        }
    }

    public static class TheInstance {

        private final TheSingleton singleton;

        @Inject
        private TheInstance(TheSingleton singleton) {
            this.singleton = singleton;
        }
    }
}
