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

import static org.usrz.libs.inject.utils.Parameters.notNull;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class InjectorTest extends AbstractTest {

    private final Map<String, String> map = Collections.singletonMap("foo", "bar");
    private final Key<Map<String, String>> mapKey = Key.of(new TypeLiteral<Map<String,String>>(){});
    private final Key<TestObject<String>> selectorKey = Key.of(new TypeLiteral<TestObject<String>>(){});

    @Test
    public void testConstructor() {
        final TestObject<String> selector = Injector.create((binder) -> {
            binder.bind(mapKey).toInstance(map);
            binder.bind(selectorKey).to(new TypeLiteral<ConstructorTestObject<String>>(){});
        }).getInstance(selectorKey);

        assertNotNull(selector, "Null selector");
        assertEquals(selector.getFromMap("foo"), "bar", "Wrong value selected");
    }

    @Test
    public void testField() {
        final TestObject<String> selector = Injector.create((binder) -> {
            binder.bind(mapKey).toInstance(map);
            binder.bind(selectorKey).to(new TypeLiteral<FieldTestObject<String>>(){});
        }).getInstance(selectorKey);

        assertNotNull(selector, "Null selector");
        assertEquals(selector.getFromMap("foo"), "bar", "Wrong value selected");
    }

    @Test
    public void testMethod() {
        final TestObject<String> selector = Injector.create((binder) -> {
            binder.bind(mapKey).toInstance(map);
            binder.bind(selectorKey).to(new TypeLiteral<MethodTestObject<String>>(){});
        }).getInstance(selectorKey);

        assertNotNull(selector, "Null selector");
        assertEquals(selector.getFromMap("foo"), "bar", "Wrong value selected");
    }

    /* ====================================================================== */

    public interface TestObject<T> {

        public T getFromMap(String name);

    }

    public static class ConstructorTestObject<T> implements TestObject<T> {

        private final Map<String, T> map;

        @Inject
        private ConstructorTestObject(Map<String, T> map) {
            this.map = notNull(map, "Null map on constructor");
        }

        @Override
        public T getFromMap(String name) {
            return notNull(map, "Null map on select").get(name);
        }

    }

    public static class FieldTestObject<T> implements TestObject<T> {

        @Inject private Map<String, T> map;

        public FieldTestObject() {}

        @Override
        public T getFromMap(String name) {
            return notNull(map, "Null map on select").get(name);
        }

    }

    public static class MethodTestObject<T> implements TestObject<T> {

        private Map<String, T> map;

        @Inject
        private void setMap(Map<String, T> map) {
            this.map = notNull(map, "Null map on setter");
        }

        @Override
        public T getFromMap(String name) {
            return notNull(map, "Null map on select").get(name);
        }

    }

}
