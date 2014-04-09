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

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;

import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.ConfigurationsBuilder;
import org.usrz.libs.testing.AbstractTest;

public class ConfigurationsTest extends AbstractTest {

    @Test
    public void testConfigurations() {
        final Configurations configurations = new ConfigurationsBuilder()
                                .put("number", "1234567890 ")
                                .put("uri", "http://www.usrz.com/")
                                .build();
        final Configurable configurable = Injector.create((binder)->binder.configure(configurations)).getInstance(Configurable.class);
        assertEquals(configurable.number, 1234567890);
        assertEquals(configurable.uri, URI.create("http://www.usrz.com/"));
        assertEquals(configurable.string, "a default value");
    }

    @Test
    public void testScopedConfigurations() {
        final Configurations parent = new ConfigurationsBuilder()
                                .put("number", "1234567890 ")
                                .put("uri", "http://www.usrz.com/")
                                .build();
        final Configurations child = new ConfigurationsBuilder()
                                .put("number", "0987654321")
                                .put("string", "overridden")
                                .build();

        final Configurable configurable = Injector.create((binder)-> {
            binder.configure(parent);
            binder.isolate((binder2) -> {
                binder2.configure(child);
                binder2.bind(Configurable.class);
                binder2.expose(Configurable.class);
            });
        }).getInstance(Configurable.class);

        assertEquals(configurable.number, 987654321);
        assertEquals(configurable.uri, URI.create("http://www.usrz.com/"));
        assertEquals(configurable.string, "overridden");
    }

    /* ====================================================================== */

    public static class Configurable {

        @Inject @Named("number") private int number;
        @Inject @Named("string") @Optional private String string = "a default value";
        @Inject @Named("uri") private URI uri;

        @SuppressWarnings("unused")
        private void preventEclipseToMarkStringAsFinal() {
            string = null;
        }

    }

    /* ====================================================================== */

    @Test
    public void testNeedsConfigurationOK() {
        Injector.create((binder) -> binder.configure(Configurations.EMPTY_CONFIGURATIONS)).getInstance(NeedsConfigurations.class);
    }

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s)Unable to create dynamic binding.*Dependency.*Configurations.*Binding.*NeedsConfigurations.*")
    public void testNeedsConfigurationNO() {
        Injector.create().getInstance(NeedsConfigurations.class);
    }

    public static class NeedsConfigurations {
        @Inject private NeedsConfigurations(Configurations configurations) {}
    }

}
