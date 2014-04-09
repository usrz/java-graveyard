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

import javax.inject.Named;

import org.testng.annotations.Test;
import org.usrz.libs.inject.utils.Names;
import org.usrz.libs.testing.AbstractTest;

public class KeyTest extends AbstractTest {

    @Test
    public void testGoodKey() {
        final Key<Object> key = Key.of(Object.class, Names.named("foobar"));
        assertEquals(key.getTypeLiteral().getRawClass(), Object.class);
        assertEquals(key.getTypeLiteral().getParameters().size(), 0);
        assertEquals(key.getAnnotationType(), Named.class);
        assertEquals(((Named)key.getAnnotation()).value(), "foobar");
        assertTrue(key.toString().startsWith("Key[Object,annotation=@javax.inject.Named(value=foobar)]@"), "Wrong string " + key.toString());
    }

    @Test(expectedExceptions=TypeException.class,
          expectedExceptionsMessageRegExp="(?s)Non-marker annotation must be fully specified \\(needs an instance\\).*javax.inject.Named.*")
    public void testNonMarkerKey() {
        Key.of(Object.class, Named.class);
    }

    @Test(expectedExceptions=TypeException.class,
            expectedExceptionsMessageRegExp="(?s)Annotation is not annotated with @Qualifier.*java.lang.Deprecated.*")
      public void testNonQualifierKey() {
          Key.of(Object.class, Deprecated.class);
      }

}
