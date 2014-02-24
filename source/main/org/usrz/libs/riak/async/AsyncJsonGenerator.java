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

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.Body;
import com.ning.http.client.BodyGenerator;
import com.ning.http.client.generators.ByteArrayBodyGenerator;

public class AsyncJsonGenerator implements BodyGenerator {

    private final Future<byte[]> future;

    public AsyncJsonGenerator(AsyncRiakClient client, Object value) {
        final Generator generator = new Generator(client.getObjectMapper(), value);
        future = client.getExecutorService().submit(generator);
    }

    @Override
    public Body createBody()
    throws IOException {
        try {
            return new ByteArrayBodyGenerator(future.get()).createBody();
        } catch (InterruptedException exception) {
            throw new IllegalStateException("Interrupted", exception);
        } catch (ExecutionException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof Error) throw (Error) cause;
            if (cause instanceof IOException) throw (IOException) cause;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new IOException("Exception generating content", cause);
        }
    }

    /* ====================================================================== */

    public class Generator implements Callable<byte[]> {

        private final ObjectMapper mapper;
        private final Object object;

        public Generator(ObjectMapper mapper, Object object) {
            this.mapper = mapper;
            this.object = object;
        }

        @Override
        public byte[] call()
        throws Exception {
            return mapper.writeValueAsBytes(object);
        }


    }

}
