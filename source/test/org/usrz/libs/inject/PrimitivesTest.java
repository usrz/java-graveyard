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

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class PrimitivesTest extends AbstractTest {

    @Test
    public void testObjetsToPrimitives() {

        final ObjectToPrimitives instance  = Injector.create((binder) -> {
            binder.bind(Boolean.class)  .toInstance(true);
            binder.bind(Byte.class)     .toInstance((byte) 12);
            binder.bind(Short.class)    .toInstance((short) 1234);
            binder.bind(Character.class).toInstance('*');
            binder.bind(Integer.class)  .toInstance(12345678);
            binder.bind(Long.class)     .toInstance(1234567890123456L);
            binder.bind(Float.class)    .toInstance(1.234F);
            binder.bind(Double.class)   .toInstance(9.87654321D);
        }).getInstance(ObjectToPrimitives.class);

        assertEquals(instance._boolean, true, "Wrong boolean");
        assertEquals(instance._byte   , (byte) 12, "Wrong byte");
        assertEquals(instance._short  , (short) 1234, "Wrong short");
        assertEquals(instance._char   , '*', "Wrong char");
        assertEquals(instance._int    , 12345678, "Wrong int");
        assertEquals(instance._long   , 1234567890123456L, "Wrong long");
        assertEquals(instance._float  , 1.234F, "Wrong float");
        assertEquals(instance._double , 9.87654321D, "Wrong double");

    }

    public static class ObjectToPrimitives {

        @Inject private boolean _boolean;
        @Inject private byte    _byte;
        @Inject private short   _short;
        @Inject private char    _char;
        @Inject private int     _int;
        @Inject private long    _long;
        @Inject private float   _float;
        @Inject private double  _double;

    }

    @Test
    public void testPrimitivesToObject() {

        final PrimitivesToObjects instance  = Injector.create((binder) -> {
            binder.bind(boolean.class).toInstance(true);
            binder.bind(byte.class)   .toInstance((byte) 12);
            binder.bind(short.class)  .toInstance((short) 1234);
            binder.bind(char.class)   .toInstance('*');
            binder.bind(int.class)    .toInstance(12345678);
            binder.bind(long.class)   .toInstance(1234567890123456L);
            binder.bind(float.class)  .toInstance(1.234F);
            binder.bind(double.class) .toInstance(9.87654321D);
        }).getInstance(PrimitivesToObjects.class);

        assertEquals(instance._boolean, Boolean.valueOf(true), "Wrong boolean");
        assertEquals(instance._byte   , Byte.valueOf((byte) 12), "Wrong byte");
        assertEquals(instance._short  , Short.valueOf((short) 1234), "Wrong short");
        assertEquals(instance._char   , Character.valueOf('*'), "Wrong char");
        assertEquals(instance._int    , Integer.valueOf(12345678), "Wrong int");
        assertEquals(instance._long   , Long.valueOf(1234567890123456L), "Wrong long");
        assertEquals(instance._float  , Float.valueOf(1.234F), "Wrong float");
        assertEquals(instance._double , Double.valueOf(9.87654321D), "Wrong double");
    }

    public static class PrimitivesToObjects {

        @Inject private Boolean   _boolean;
        @Inject private Byte      _byte;
        @Inject private Short     _short;
        @Inject private Character _char;
        @Inject private Integer   _int;
        @Inject private Long      _long;
        @Inject private Float     _float;
        @Inject private Double    _double;

    }

    @Test(expectedExceptions=TypeException.class,
          expectedExceptionsMessageRegExp="(?s)Annotation @Nullable applied to primitive int.*NullablePrimitive.*")
    public void testNullablePrimitive() {
        try {
            Injector.create().getInstance(NullablePrimitive.class);
        } catch (ProvisionException exception) {
            throw (TypeException) exception.getCause();
        }
    }

    public static class NullablePrimitive {
        @Inject @Nullable private int failme;
    }

}
