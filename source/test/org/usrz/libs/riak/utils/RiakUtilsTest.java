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
package org.usrz.libs.riak.utils;

import org.testng.annotations.Test;
import org.usrz.libs.riak.utils.RiakUtils;
import org.usrz.libs.testing.AbstractTest;

public class RiakUtilsTest extends AbstractTest {

    @Test
    public void testEncoding() {
        final String original = "The string \u00FC@foo-bar";
        final String encoded = RiakUtils.encode(original);
        assertEquals(encoded, "The+string+%C3%BC%40foo-bar");
        final String decoded = RiakUtils.decode(encoded);
        assertEquals(decoded, original);

        try { RiakUtils.decode("Hello%XXFail"); fail("No exception"); } catch (IllegalArgumentException ok) {}
        try { RiakUtils.decode("Hello%a_Fail"); fail("No exception"); } catch (IllegalArgumentException ok) {}
        try { RiakUtils.decode("HelloWorld%3"); fail("No exception"); } catch (IllegalArgumentException ok) {}

        /* Invalid UTF, note the replacement character, and NOT a question mark :-) */
        assertEquals(RiakUtils.decode("This %c3%28 is %e2%28%a1 invalid %e2%82%28 in %f0%28%8c%bc UTF8"),
                     "This \ufffd( is \ufffd(\ufffd invalid \ufffd( in \ufffd(\ufffd\ufffd UTF8");
    }

}
