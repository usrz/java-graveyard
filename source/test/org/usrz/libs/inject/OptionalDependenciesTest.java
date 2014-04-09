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
import java.util.Map;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class OptionalDependenciesTest extends AbstractTest {

    private static final Map<String, Integer> REFERENCE = new HashMap<>();

    /* ====================================================================== */

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s)Unable to create dynamic binding.*Requirer.*")
    public void testOptionalDependencies() {
        Injector.create().getInstance(Requirer.class);
    }

    public static class Requirer {
        @Inject
        public Requirer(Map<String, Integer> map) {
            Assert.fail("Constructor invoked");
        }
    }

    /* ====================================================================== */

    @Test
    public void testConstructor1() {
        final Injector injector = Injector.create();
        assertNotNull(injector.getInstance(NonRequirerConstructor.class));
    }

    public static class NonRequirerConstructor {
        @Inject
        public NonRequirerConstructor(@Optional Map<String, Integer> map) {
            Assert.assertNull(map, "Non-null map received");
        }
    }

    /* ====================================================================== */

    @Test
    public void testConstructor2() {
        final Injector injector = Injector.create();
        assertNotNull(injector.getInstance(NonRequirerConstructor2.class));
    }

    public static class NonRequirerConstructor2 {
        @Inject
        public NonRequirerConstructor2(@Optional Map<String, Integer> map, Injector injector) {
            Assert.assertNull(map, "Non-null map received");
            Assert.assertNotNull(injector, "Null injector received");
        }
    }

    /* ====================================================================== */

    @Test
    public void testField1() {
        final Injector injector = Injector.create();
        NonRequirerField instance = injector.getInstance(NonRequirerField.class);
        assertNotNull(instance);
        assertSame(instance.map, REFERENCE, "Dependency was set");
    }

    public static class NonRequirerField {

        @Inject
        @Optional
        private Map<String, Integer> map = REFERENCE;

        public void stupidMethodNotToMakeEclipseSetTheFieldToFinal() {
            map = null;
        }
    }

    /* ====================================================================== */

    @Test
    public void testMethod1() {
        final Injector injector = Injector.create();
        assertNotNull(injector.getInstance(NonRequirerMethod.class));
    }

    public static class NonRequirerMethod {

        @Inject
        @Optional
        public void setMap(Map<String, Integer> map) {
            Assert.assertNull("The setMap() method should NOT have been callsed");
        }
    }

    /* ====================================================================== */

    @Test
    public void testMethod2() {
        final Injector injector = Injector.create();
        NonRequirerMethod2 instance = injector.getInstance(NonRequirerMethod2.class);
        assertNotNull(instance);
        assertTrue(instance.invoked, "Method invoked");
    }

    public static class NonRequirerMethod2 {

        public boolean invoked = false;

        @Inject
        public void setMap(@Optional Map<String, Integer> map) {
            invoked = true;
            Assert.assertNull(map, "Non-null map received");
        }
    }

    /* ====================================================================== */

    @Test
    public void testMethod3() {
        final Injector injector = Injector.create();
        NonRequirerMethod3 instance = injector.getInstance(NonRequirerMethod3.class);
        assertNotNull(instance);
        assertTrue(instance.invoked, "Method invoked");
    }

    public static class NonRequirerMethod3 {

        public boolean invoked = false;

        @Inject
        @Optional
        public void setMap(Map<String, Integer> map, Injector injector) {
            invoked = true;
            Assert.assertNull(map, "Non-null map received");
            Assert.assertNotNull(injector, "Null injector received");
        }
    }

}
