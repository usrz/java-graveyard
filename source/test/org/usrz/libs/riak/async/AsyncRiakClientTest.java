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
package org.usrz.libs.riak.async;

import java.util.concurrent.Future;

import org.testng.annotations.Test;
import org.usrz.libs.riak.Bucket;
import org.usrz.libs.riak.Reference;
import org.usrz.libs.riak.Response;
import org.usrz.libs.riak.RiakClient;
import org.usrz.libs.riak.async.AsyncRiakClient;
import org.usrz.libs.testing.AbstractTest;

import com.ning.http.client.AsyncHttpClient;

public class AsyncRiakClientTest extends AbstractTest {

    @Test
    public void testGetBuckets()
    throws Exception {
        final AsyncRiakClient client = new AsyncRiakClient(new AsyncHttpClient());

        for (Bucket bucket: client.getBuckets()) {
            System.err.println("BUCKET ==> \"" + bucket + "\"");
            for (Reference reference: bucket.getReferences()) {
                System.err.println("       --> \"" + reference + "\"");
            }
        }

        System.err.println("DONE!!!!!!");

    }

    //@Test
    public void testStore()
    throws Exception {
        final RiakClient client = new AsyncRiakClient(new AsyncHttpClient());
        final Bucket bucket = client.getBucket("test");

        final TestObject object = new TestObject();

        object.setValue("foobar");
        final Future<Response<TestObject>> future = bucket.store(object).execute();
        final Response<TestObject> response = future.get();

        object.setValue("foobar2");
        final Future<Response<TestObject>> future2 = bucket.store(object, response.getReference().getKey()).execute();
        final Response<TestObject> response2 = future2.get();

        final Future<Response<Void>> delete = bucket.delete(response.getReference().getKey()).execute();
        final Response<Void> deleteResponse = delete.get();


    }

    public static class TestObject {

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }
}
