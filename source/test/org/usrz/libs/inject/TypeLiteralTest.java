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

import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class TypeLiteralTest extends AbstractTest {

    private void check(TypeLiteral<?> literal, Class<?> rawClass) {
        check(literal, rawClass, new TypeLiteral<?>[0]);
    }

    private void check(TypeLiteral<?> literal, Class<?> rawClass, Class<?>... parameters) {
        final TypeLiteral<?>[] literals = new TypeLiteral<?>[parameters.length];
        for (int x = 0; x < parameters.length; x ++) literals[x] = TypeLiteral.of(parameters[x]);
        this.check(literal, rawClass, literals);
    }

    private void check(TypeLiteral<?> literal, Class<?> rawClass, TypeLiteral<?>... parameters) {
        assertNotNull(literal, "Null literal specified");
        assertNotNull(rawClass, "Null raw class specified");
        assertNotNull(parameters, "Null parameters specified");
        assertNotNull(literal.getRawClass(), "Null raw type for literal " + literal);
        assertEquals(literal.getRawClass(), rawClass);
        assertEquals(literal.getParameters().size(), parameters.length, "Invalid number of parameters");
        for (int x = 0; x < parameters.length; x ++) {
            assertEquals(literal.getParameters().get(x), parameters[x], "Invalid parameter at index " + x);
        }
    }

    /* ====================================================================== */

    @Test
    public <T> void testSimpleTypeLiteral() {
        check(new TypeLiteral<String>(){}, String.class);
        check(new TypeLiteral<Map<String, Integer>>(){}, Map.class, String.class, Integer.class);
        check(new TypeLiteral<Map<String, Set<Integer>>>(){}, Map.class, new TypeLiteral<String>(){}, new TypeLiteral<Set<Integer>>(){});
        check(new TypeLiteral<byte[]>(){}, byte[].class);
        check(new TypeLiteral<Set<String>[]>(){}, Set[].class, String.class);
        check(new TypeLiteral<Map<String, Set<Integer>[]>[]>(){}, Map[].class, new TypeLiteral<String>(){}, new TypeLiteral<Set<Integer>[]>(){});
    }

    @Test
    public <T> void testSimpleTypeLiteralToString() {
        final TypeLiteral<Map<String, Set<Integer>[]>> literal = new TypeLiteral<Map<String, Set<Integer>[]>>(){};
        assertTrue(literal.toString().startsWith("TypeLiteral[Map<String,Set<Integer>[]>]@"), "Wrong string " + literal.toString());
    }

    @Test
    public <T> void testSimpleTypeLiteralOf() {
        check(TypeLiteral.of(boolean.class), Boolean.class);
        check(TypeLiteral.of(byte.class),    Byte.class);
        check(TypeLiteral.of(short.class),   Short.class);
        check(TypeLiteral.of(char.class),    Character.class);
        check(TypeLiteral.of(int.class),     Integer.class);
        check(TypeLiteral.of(long.class),    Long.class);
        check(TypeLiteral.of(float.class),   Float.class);
        check(TypeLiteral.of(double.class),  Double.class);
        check(TypeLiteral.of(String.class),  String.class);
    }

    @Test(expectedExceptions=TypeException.class,
          expectedExceptionsMessageRegExp="(?s)Unable to resolve ype variable T declared on.*TypeLiteralTest.testWrongTypeLiteral.*TypeLiteralTest.*")
    public <T> void testWrongTypeLiteral() {
        new TypeLiteral<T>(){};
    }

    /* ====================================================================== */

    public abstract class MyProvider<T> implements Provider<T> {

    }

    public abstract class StringProvider implements Provider<String> {

    }
}
