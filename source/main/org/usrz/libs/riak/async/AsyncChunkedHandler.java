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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.usrz.libs.logging.Log;
import org.usrz.libs.riak.utils.QueuedIterableFuture;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Request;

public class AsyncChunkedHandler implements AsyncHandler<Void> {

    private static final Log log = new Log();

    private final QueuedIterableFuture<String> iterable = new QueuedIterableFuture<>();
    private final AsyncRiakClient client;
    private final Request request;

    private OutputStream output;
    private Future<?> future;

    public AsyncChunkedHandler(AsyncRiakClient client, Request request) {
        log.trace("Chunked Content Handler %s created", request.getUrl());
        this.request = request;
        this.client = client;
    }

    protected QueuedIterableFuture<String> getFuture() {
        return iterable;
    }

    @Override
    public STATE onStatusReceived(HttpResponseStatus status)
    throws Exception {
        /* Don't check for status, just go */
        log.trace("Received status for %s: %d", status.getUrl(), status.getStatusCode());
        if (status.getStatusCode() == 200) {
            final PipedInputStream input = new PipedInputStream();
            output = new PipedOutputStream(input);
            future = client.getExecutorService().submit(new Parser(input));
            iterable.addFuture(future);

            return STATE.CONTINUE;
        } else {
            return STATE.ABORT;
        }
    }

    @Override
    public STATE onHeadersReceived(HttpResponseHeaders headers)
    throws Exception {
        log.trace("Received headers for request %s", headers.getUrl());
        return STATE.CONTINUE;
    }

    @Override
    public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart)
    throws Exception {
        log.trace("Received body part for request %s", bodyPart.getUrl());
        if (output != null) bodyPart.writeTo(output);
        return STATE.CONTINUE;
    }

    @Override
    public Void onCompleted() throws Exception {
        log.trace("Completed processing of %s", request.getUrl());

        if (output != null) try {
            output.close();
        } catch (Throwable throwable) {
            if (future != null) future.cancel(true);
            iterable.fail(throwable);
            onThrowable(throwable);
        }

        return null;
    }

    @Override
    public void onThrowable(Throwable throwable) {
        log.error(throwable, "Exception detected for %s", request.getUrl());
    }

    /* ====================================================================== */

    public class Parser implements Callable<Void> {

        private final InputStream input;

        public Parser(InputStream input) {
            this.input = input;
        }

        @Override
        public Void call()
        throws Exception {
            try {
                final ObjectMapper mapper = client.getObjectMapper();
                final JsonParser parser = mapper.getFactory().createParser(input);
                final MappingIterator<JsonNode> iterator = mapper.readValues(parser, JsonNode.class);
                while (iterator.hasNextValue()) {
                    final JsonNode bucketsOrKeys = iterator.next();
                    for (JsonNode arrayOrString: bucketsOrKeys) {
                        if (arrayOrString.isTextual()) {
                            iterable.put(arrayOrString.asText());
                        } else if (arrayOrString.isArray()) {
                            for (JsonNode arrayValue: arrayOrString) {
                                iterable.put(arrayValue.asText());
                            }
                        }
                    }
                }
                iterable.close();
                return null;
            } catch (Exception exception) {
                iterable.fail(exception);
                throw exception;
            } finally {
                input.close();
            }
        }
    }

}
