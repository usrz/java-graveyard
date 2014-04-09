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

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Provider;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class ProviderTest extends AbstractTest {

    @Test
    public void testProvider() {
        final Injector injector = Injector.create((binder) -> {
            binder.bind(String.class).toProvider(() -> "foo");
        });
        assertEquals(injector.getInstance(String.class), "foo");
    }

    @Test(expectedExceptions = ProvisionException.class,
          expectedExceptionsMessageRegExp = "(?s)Providers must be bound to the type they produce.*Provider<String>.*")
    public void testBindProvider() {
        final Injector injector = Injector.create((binder) -> {
            binder.bind(new TypeLiteral<Provider<String>>(){}).toInstance(() -> "foo");
        });
        assertEquals(injector.getInstance(String.class), "foo");
    }

    /* ====================================================================== */

    @Test
    public void testProviderRequired() {
        final Injector injector = Injector.create((binder)-> binder.bind(Double.class).toInstance(123.456));
        assertEquals(injector.getInstance(RequiresProvider.class).number, 123.456);
    }

    private static class RequiresProvider {

        private final double number;

        @Inject
        public RequiresProvider(Provider<Double> provider) {
            Assert.assertNotNull(provider);
            number = provider.get();
        }
    }

    /* ====================================================================== */

    @Test
    public void testProviderInstance() {
        final int number = NumberProvider.staticCount.get();
        final Injector injector = Injector.create((binder) -> binder.bind(String.class).toProvider(new NumberProvider()));

        assertEquals(injector.getInstance(String.class), number + "/" + 0);
        assertEquals(injector.getInstance(String.class), number + "/" + 1);
        assertEquals(injector.getInstance(String.class), number + "/" + 2);
    }

    @Test
    public void testProviderType() {
        int number = NumberProvider.staticCount.get();
        final Injector injector = Injector.create((binder) -> binder.bind(String.class).toProvider(NumberProvider.class));

        assertEquals(injector.getInstance(String.class), (number ++) + "/" + 0);
        assertEquals(injector.getInstance(String.class), (number ++) + "/" + 0);
        assertEquals(injector.getInstance(String.class), (number ++) + "/" + 0);
    }

    public static class NumberProvider implements Provider<String> {

        public static final AtomicInteger staticCount = new AtomicInteger();
        public final AtomicInteger instanceCount = new AtomicInteger();
        private final int providerNumber;

        @Inject
        private NumberProvider() {
            providerNumber = staticCount.getAndIncrement();
        }

        @Override
        public String get() {
            return providerNumber + "/" + instanceCount.getAndIncrement();
        }
    }

}
