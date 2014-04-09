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

import java.util.Enumeration;
import java.util.function.Consumer;

import junit.framework.TestFailure;
import junit.framework.TestResult;

import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.SpareTire;
import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

@SuppressWarnings("restriction")
public class JSR330TCKTest extends AbstractTest {

    /*
     * Configure the injector as follows:
     *
     * - Car is implemented by Convertible.
     * - @Drivers Seat is implemented by DriversSeat.
     * - Seat is implemented by Seat itself, and Tire by Tire itself (not subclasses).
     * - Engine is implemented by V8Engine.
     * - @Named("spare") Tire is implemented by SpareTire.
     * - The following classes may also be injected directly: Cupholder, SpareTire, and FuelTank.
     *
     * Static and private member injection support is optional, but if your injector supports
     * those features, it must pass the respective tests. If static member injection is supported,
     * the static members of the following types shall also be injected once: Convertible, Tire,
     * and SpareTire.
     */
    private final Consumer<Binder> module = (binder) -> {
        binder.bind(Car.class).to(Convertible.class);
        binder.bind(Seat.class).with(Drivers.class).to(DriversSeat.class);
        binder.bind(Engine.class).to(V8Engine.class);
        binder.bind(Tire.class).with("spare").to(SpareTire.class);
    };

    private TestResult testTck(Car car, boolean supportsStatic) {
        assertNotNull(car, "The Car to be tested is null");

        final TestResult result = new TestResult();

        /* Always support private injection, static injection optional */
        Tck.testsFor(car, supportsStatic, true).run(result);

        log.info("Ran a total of %d tests (%d errors, %d failures)",
                 result.runCount(),
                 result.errorCount(),
                 result.failureCount());

        final Enumeration<TestFailure> errors = result.errors();
        while(errors.hasMoreElements()) {
            final TestFailure error = errors.nextElement();
            log.warn("TCK Test %s error: %s",
                     error.failedTest().toString(),
                     error.exceptionMessage());
        }

        final Enumeration<TestFailure> failures = result.failures();
        while(failures.hasMoreElements()) {
            final TestFailure failure = failures.nextElement();
            log.warn("TCK Test %s failed: %s",
                     failure.failedTest().toString(),
                     failure.exceptionMessage());
        }

        return result;
    }

    @Test(priority=3)
    public void testTckNonStatic() {
        final TestResult result = testTck(Injector.create(module).getInstance(Car.class), false);
        assertTrue(result.wasSuccessful(), "TCK failed with " +  result.errorCount() + " errors and " +  result.failureCount() + " failures");
    }

    @Test(priority=2)
    public void testTckStatic() {
        final TestResult result = testTck(Injector.create(true, module).getInstance(Car.class), true);
        assertTrue(result.wasSuccessful(), "TCK failed with " +  result.errorCount() + " errors and " +  result.failureCount() + " failures");
    }

    @Test(priority=1)
    public void testTckStaticDisabled() {
        final TestResult result = testTck(Injector.create(module).getInstance(Car.class), true);
        assertFalse(result.wasSuccessful(), "TCK succeded with static injection disabled");
        assertEquals(result.errorCount(), 0, "Wrong number of errors from TCK");
        assertEquals(result.failureCount(), 8, "Wrong number of failures from TCK");
    }

}
