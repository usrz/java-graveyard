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

public class KeyTest extends AbstractTest {

    private final RiakClient client = new FakeClient();

    @Test
    public void testEncoded() {
        final Key key = new Key(client, "/buckets/dev%40usrz/keys/f%C3%BCbar");
        assertEquals(key.getBucketName(), "dev@usrz");
        assertEquals(key.getName(), "f\u00FCbar");
    }

    @Test
    public void testKeys() {
        for (String string: new String[] { "/riak/mybucket/mykey",
                                           "/riak/mybucket/mykey/",
                                           "/buckets/mybucket/keys/mykey",
                                           "/buckets/mybucket/keys/mykey/",
                                           "http://127.0.0.1:1234/riak/mybucket/mykey",
                                           "http://127.0.0.1:1234/riak/mybucket/mykey/",
                                           "http://127.0.0.1:1234/buckets/mybucket/keys/mykey",
                                           "http://127.0.0.1:1234/buckets/mybucket/keys/mykey/" }) {
            final Key key1 = new Key(client, string);
            assertEquals(key1.getBucketName(), "mybucket");
            assertEquals(key1.getName(), "mykey");

            final Key key2 = new Key(client, string + "?foo=bar");
            assertEquals(key2.getBucketName(), "mybucket");
            assertEquals(key2.getName(), "mykey");
        }
    }
}
