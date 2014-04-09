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

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Provider;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class NullableDependenciesTest extends AbstractTest {

    private static final TypeLiteral<Map<String, Integer>> TYPE = new TypeLiteral<Map<String, Integer>>(){};
    private static final Provider<Map<String, Integer>> PROVIDER = () -> null;
    private static final Consumer<Binder> MODULE = (binder) -> binder.bind(TYPE).toProvider(PROVIDER);

    /* ====================================================================== */

    @Test
    public void testConstructor1A() {
        final Injector injector = Injector.create(MODULE);
        assertNotNull(injector.getInstance(NonRequirerConstructor1.class));
    }

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s)Unable to create dynamic binding.*NonRequirerConstructor1.*")
    public void testConstructor1B() {
        Injector.create().getInstance(NonRequirerConstructor1.class);
    }

    public static class NonRequirerConstructor1 {
        @Inject
        public NonRequirerConstructor1(@Nullable Map<String, Integer> map) {
            Assert.assertNull(map, "Non-null map received");
        }
    }

    /* ====================================================================== */

    @Test
    public void testConstructor2A() {
        final Injector injector = Injector.create(MODULE);
        assertNotNull(injector.getInstance(NonRequirerConstructor2.class));
    }

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s)Unable to create dynamic binding.*NonRequirerConstructor2.*")
    public void testConstructor2B() {
        Injector.create().getInstance(NonRequirerConstructor2.class);
    }

    public static class NonRequirerConstructor2 {
        @Inject
        public NonRequirerConstructor2(@Nullable Map<String, Integer> map, Injector injector) {
            Assert.assertNull(map, "Non-null map received");
            Assert.assertNotNull(injector, "Null injector received");
        }
    }

    /* ====================================================================== */

    @Test
    public void testField1A() {
        final Injector injector = Injector.create(MODULE);
        NonRequirerField instance = injector.getInstance(NonRequirerField.class);
        assertNotNull(instance);
        assertNull(instance.map, "Dependency was not set to null");
    }

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s)Unable to create dynamic binding.*NonRequirerField.*")
    public void testField1B() {
        Injector.create().getInstance(NonRequirerField.class);
    }

    public static class NonRequirerField {

        @Inject @Nullable private Map<String, Integer> map = Collections.emptyMap();

        public void stupidMethodNotToMakeEclipseSetTheFieldToFinal() {
            map = null;
        }
    }

    /* ====================================================================== */

    @Test
    public void testMethod1A() {
        final Injector injector = Injector.create(MODULE);
        NonRequirerMethod1 instance = injector.getInstance(NonRequirerMethod1.class);
        assertNotNull(instance);
        assertTrue(instance.invoked, "Method not invoked");
    }

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s)Unable to create dynamic binding.*NonRequirerMethod1.*")
    public void testMethod1B() {
        Injector.create().getInstance(NonRequirerMethod1.class);
    }

    public static class NonRequirerMethod1 {

        public boolean invoked = false;

        @Inject
        public void setMap(@Nullable Map<String, Integer> map, Injector injector) {
            invoked = true;
            Assert.assertNull(map, "Non-null map received");
            Assert.assertNotNull(injector, "Null injector received");
        }
    }

    /* ====================================================================== */

    @Test
    public void testMethod2A() {
        final Injector injector = Injector.create(MODULE);
        NonRequirerMethod2 instance = injector.getInstance(NonRequirerMethod2.class);
        assertNotNull(instance);
        assertTrue(instance.invoked, "Method not invoked");
    }

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s)Unable to create dynamic binding.*NonRequirerMethod2.*")
    public void testMethod2B() {
        Injector.create().getInstance(NonRequirerMethod2.class);
    }

    public static class NonRequirerMethod2 {

        public boolean invoked = false;

        @Inject
        public void setMap(@Nullable Map<String, Integer> map) {
            invoked = true;
            Assert.assertNull(map, "Non-null map received");
        }
    }
}
