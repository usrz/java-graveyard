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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;

@SuppressWarnings("restriction")
public class PerformanceTest extends AbstractTest {

    private void runGuice(int iterations) {
        final com.google.inject.Injector injector = Guice.createInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Car.class).to(Convertible.class);
                        bind(Seat.class).annotatedWith(Drivers.class).to(DriversSeat.class);
                        bind(Engine.class).to(V8Engine.class);
                        bind(Tire.class).annotatedWith(Names.named("spare")).to(SpareTire.class);
                    };
                });

        for (int x = 0; x < iterations; x ++) {
            final TestResult result = new TestResult();
            Tck.testsFor(injector.getInstance(Car.class), false, true).run(result);
            assertTrue(result.wasSuccessful());
        }
    }

    private void runUsrz(int iterations) {
        final Injector injector = Injector.create((binder) -> {
            binder.bind(Car.class).to(Convertible.class);
            binder.bind(Seat.class).with(Drivers.class).to(DriversSeat.class);
            binder.bind(Engine.class).to(V8Engine.class);
            binder.bind(Tire.class).with("spare").to(SpareTire.class);
        });

        for (int x = 0; x < iterations; x ++) {
            final TestResult result = new TestResult();
            Tck.testsFor(injector.getInstance(Car.class), false, true).run(result);
            assertTrue(result.wasSuccessful());
        }
    }

    @Test
    public void testPerformance() throws InterruptedException {
        final int iterations = 500;

        /* Warmup phase */
        runGuice(iterations);
        runUsrz(iterations);

        System.gc();
        Thread.sleep(1000);

        final long guiceNanoStart = System.nanoTime();
        runGuice(iterations);
        final long guiceNanos = System.nanoTime() - guiceNanoStart;

        System.gc();
        Thread.sleep(1000);

        final long usrzNanoStart = System.nanoTime();
        runUsrz(iterations);
        final long usrzNanos = System.nanoTime() - usrzNanoStart;

        log.info("Guice performance: %10d nanos", guiceNanos);
        log.info("Local performance: %10d nanos", usrzNanos);

    }
}
