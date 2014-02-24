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
package org.usrz.libs.riak;

import static org.usrz.libs.riak.IndexType.BINARY;
import static org.usrz.libs.riak.IndexType.INTEGER;

import org.testng.annotations.Test;
import org.usrz.libs.logging.Log;
import org.usrz.libs.testing.AbstractTest;

public class IndexTest extends AbstractTest {

    private final Log log = new Log();

    @Test
    public void testIndexType() {
        final Index e1 = new Index("X-Riak-Index-MyField_bin");
        assertEquals(e1.getName(), "myfield");
        assertEquals(e1.getType(), BINARY);

        final Index e2 = new Index("x-riak-index-AnotherField_int");
        assertEquals(e2.getName(), "anotherfield");
        assertEquals(e2.getType(), INTEGER);

        final Index e3 = new Index("YetAnotherField_bin");
        assertEquals(e3.getName(), "yetanotherfield");
        assertEquals(e3.getType(), BINARY);

        final Index e4 = new Index("AndToFinishTheField_int");
        assertEquals(e4.getName(), "andtofinishthefield");
        assertEquals(e4.getType(), INTEGER);

        final Index e5 = new Index("x-riak-index-f%C3%BC%40bar_bin");
        assertEquals(e5.getName(), "f\u00FC@bar");
        assertEquals(e5.getType(), BINARY);


        for (String header: new String[] { "X-Riak-Index-Foo_bar", "foo_bar",
                                           "X-Riak-Index-_bin", "_num" }) try {
            new Index(header);
            fail("The header \"" + header + "\" should fail");
        } catch (IllegalArgumentException exception) {
            log.trace("The header \"%s\" failed as expected", header);
        }


    }

}
