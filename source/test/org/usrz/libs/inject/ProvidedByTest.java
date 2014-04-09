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

import javax.inject.Provider;

import org.testng.annotations.Test;

public class ProvidedByTest {

    @Test
    public void testProvidedByBound() {
        Injector.create((binder) -> binder.bind(Bacon.class)).getInstance(Bacon.class);
    }

    @Test
    public void testProvidedByUnound() {
        Injector.create().getInstance(Bacon.class);
    }

    @Test(expectedExceptions=TypeException.class,
          expectedExceptionsMessageRegExp="(?s)Invalid @ProvidedBy annotation.*Sausage.*")
    public void testProvidedWrongAnnotation() {
        Injector.create((binder) -> binder.bind(Sausage.class)).getInstance(Sausage.class);
    }

    @ProvidedBy(BaconProvider.class)
    public static class Bacon {
        private Bacon() { }
    }

    @ProvidedBy(BaconProvider.class)
    public static class Sausage {
        private Sausage() { }
    }

    public static class BaconProvider implements Provider<Bacon> {
        @Override public Bacon get() { return new Bacon(); }
    }
}
