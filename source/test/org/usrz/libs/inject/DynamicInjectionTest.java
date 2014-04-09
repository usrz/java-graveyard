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
import java.util.Locale;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class DynamicInjectionTest extends AbstractTest {

    @Test
    public void testDynamicInjection() {
        assertNotNull(Injector.create().getInstance(new TypeLiteral<HashMap<String,Integer>>(){}));
    }

    @Test(expectedExceptions=TypeException.class,
          expectedExceptionsMessageRegExp="(?s)No public empty or @Inject annotated constructor found.*java.util.Locale.*")
    public void testDynamicInjectionNoConstructor() {
        /* Declare it so we won't get a dynamic binding exception */
        Injector.create((binder) -> binder.bind(Locale.class)).getInstance(new TypeLiteral<Locale>(){});
    }


}
