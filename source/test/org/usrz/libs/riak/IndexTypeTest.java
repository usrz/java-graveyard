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
import org.usrz.libs.riak.Index;
import org.usrz.libs.riak.IndexType;
import org.usrz.libs.testing.AbstractTest;

public class IndexTypeTest extends AbstractTest {

    private final Log log = new Log();

    @Test
    public void testIndexType() {
        final Index e1 = IndexType.parseHeaderName("X-Riak-Index-MyField_bin");
        assertEquals(e1.getName(), "myfield");
        assertEquals(e1.getType(), BINARY);

        final Index e2 = IndexType.parseHeaderName("x-riak-index-AnotherField_int");
        assertEquals(e2.getName(), "anotherfield");
        assertEquals(e2.getType(), INTEGER);

        final Index e3 = IndexType.parseHeaderName("YetAnotherField_bin");
        assertEquals(e3.getName(), "yetanotherfield");
        assertEquals(e3.getType(), BINARY);

        final Index e4 = IndexType.parseHeaderName("AndToFinishTheField_int");
        assertEquals(e4.getName(), "andtofinishthefield");
        assertEquals(e4.getType(), INTEGER);

        for (String header: new String[] { "X-Riak-Index-Foo_bar", "foo_bar",
                                           "X-Riak-Index-_bin", "_num" }) try {
            IndexType.parseHeaderName(header);
            fail("The header \"" + header + "\" should fail");
        } catch (IllegalArgumentException exception) {
            log.trace("The header \"%s\" failed as expected", header);
        }


    }

}
