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

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class InnerClassTest extends AbstractTest {

    @Test(expectedExceptions = TypeException.class,
          expectedExceptionsMessageRegExp = "(?s)Inner classes must be static.*org.usrz.libs.inject.InnerClassTest\\$InnerClass.*")
    public void testInnerClass() {
        /* Declare it, so we won't have a DynamicBindingException */
        Injector.create((binder) -> binder.bind(InnerClass.class)).getInstance(InnerClass.class);
    }

    @Test
    public void testStaticInnerClass() {
        Injector.create().getInstance(StaticInnerClass.class);
    }

    public class InnerClass {}

    public static class StaticInnerClass {}

}
