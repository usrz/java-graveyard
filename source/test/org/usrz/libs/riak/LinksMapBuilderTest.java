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

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class LinksMapBuilderTest extends AbstractTest {

    @Test
    public void testHeaders() {
        for (String header: new String[] { "</buckets/foo2/keys/bar2>; riaktag=\"secondlink\", </buckets/foo1/keys/bar1>; riaktag=\"firstlink\", </buckets/test>; rel=\"up\"",
                                           "</buckets/foo2/keys/bar2>; riaktag=\"secondlink\", </buckets/test>; rel=\"up\", </buckets/foo1/keys/bar1>; riaktag=\"firstlink\"",
                                           "</buckets/test>; rel=\"up\", </buckets/foo2/keys/bar2>; riaktag=\"secondlink\", </buckets/foo1/keys/bar1>; riaktag=\"firstlink\"" }) {
            final LinksMap links = new LinksMapBuilder().parseHeader(header).build();
            assertTrue(links.containsValue("firstlink",  "foo1", "bar1"),  "first not found in " + header);
            assertTrue(links.containsValue("secondlink", "foo2", "bar2"), "second not found in " + header);

        }
    }

}
