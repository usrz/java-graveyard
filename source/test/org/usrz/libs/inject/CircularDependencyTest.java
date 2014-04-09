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

public class CircularDependencyTest extends AbstractTest {

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s)Circular dependency detected.*DependencyA.*DependencyB.*DependencyC.*")
    public void testCircularDependency() {
        Injector.create().getInstance(DependencyA.class);
    }

    public static class DependencyA {
        @Inject private DependencyA(DependencyB d) {}
    }

    public static class DependencyB {
        @Inject private DependencyB(DependencyC d) {}
    }

    public static class DependencyC {
        @Inject private DependencyC(DependencyA d) {}
    }

    /* ====================================================================== */

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s)Circular dependency detected.*ProvidedA.*ProvidedB.*ProvidedC.*")
    public void testCircularDependencyProviderAccessInConstructor() {
        try {
            Injector.create().getInstance(ProvidedA.class);
        } catch (Throwable exception) {
            Throwable cause = null;
            while (exception != null) {
                cause = exception;
                exception = exception.getCause();
            }
            throw (ProvisionException) cause;
        }
    }

    private static class ProvidedA {
        @Inject private ProvidedA(Provider<ProvidedB> d) { d.get(); }
    }

    private static class ProvidedB {
        @Inject private ProvidedB(Provider<ProvidedC> d) { d.get(); }
    }

    private static class ProvidedC {
        @Inject private ProvidedC(Provider<ProvidedA> d) { d.get(); }
    }

    /* ====================================================================== */

    @Test
    public void testBreakCircularDependency() {
        final Injector injector = Injector.create();
        assertNotNull(injector.getInstance(CorrectA.class).get());
        assertNotNull(injector.getInstance(CorrectB.class).get());
        assertNotNull(injector.getInstance(CorrectC.class).get());
    }

    private static class CorrectA {
        private final Provider<CorrectB> provider;
        @Inject private CorrectA(Provider<CorrectB> d) { provider = d; }
        public CorrectB get() { return provider.get(); }
    }

    private static class CorrectB {
        private final Provider<CorrectC> provider;
        @Inject private CorrectB(Provider<CorrectC> d) { provider = d; }
        public CorrectC get() { return provider.get(); }
    }

    private static class CorrectC {
        private final Provider<CorrectA> provider;
        @Inject private CorrectC(Provider<CorrectA> d) { provider = d; }
        public CorrectA get() { return provider.get(); }
    }
}
