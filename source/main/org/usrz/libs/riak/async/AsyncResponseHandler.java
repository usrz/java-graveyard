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

import java.io.OutputStream;

import org.usrz.libs.logging.Log;
import org.usrz.libs.riak.ContentHandler;
import org.usrz.libs.riak.Response;
import org.usrz.libs.riak.response.ErrorContentHandler;
import org.usrz.libs.riak.response.NullContentHandler;
import org.usrz.libs.riak.response.SiblingsContentHandler;
import org.usrz.libs.riak.utils.SettableFuture;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Request;

public class AsyncResponseHandler<T> implements AsyncHandler<Response<T>> {

    private final static Log log = new Log();

    /* See https://github.com/AsyncHttpClient/async-http-client/issues/489 */
    private final SettableFuture<Response<T>> future;
    private final ContentHandler<T> handler;
    private final AsyncRiakClient client;
    private final Request request;

    private AsyncPartialRespnse<T> partial = null;
    private OutputStream output = null;
    private int status = -1;

    protected AsyncResponseHandler(AsyncRiakClient client, Request request, ContentHandler<T> handler, SettableFuture<Response<T>> future) {
        if (client == null) throw new NullPointerException("Null client");
        if (request == null) throw new NullPointerException("Null request");
        if (handler == null) throw new NullPointerException("Null handler");
        if (future == null) throw new NullPointerException("Null future");

        log.trace("Handler for %s on %s created", request.getMethod(), request.getUrl());

        this.client = client;
        this.request = request;
        this.handler = handler;
        this.future = future;
    }

    @Override
    public STATE onStatusReceived(HttpResponseStatus status)
    throws Exception {
        final int statusCode = this.status = status.getStatusCode();
        log.trace("Received status %d for request %s", statusCode, status.getUrl());
        return STATE.CONTINUE;
    }

    @Override
    public STATE onHeadersReceived(HttpResponseHeaders headers)
    throws Exception {
        log.trace("Received headers for request %s", headers.getUrl());

        /* Create our partial response */
        this.partial = new AsyncPartialRespnse<T>(client, headers, status);

        /*
         * Statuses:
         * - Fetch Request:
         *   - 200 OK: read body
         *   - 300 Multiple Choices: siblings exception
         *   - 304 Not Modified: return null
         *   - 404 Not Found: return null
         * - Store request:
         *   - 200 OK: read body
         *   - 201 Created: on POST will give us a location
         *   - 204 No Content: on PUT when successful
         *   - 300 Multiple Choices: siblings exception
         * - Delete request:
         *   - 204 No Content: when successful
         *   - 404 Not Found: didn't delete anything
         */
        final ContentHandler<T> handler = status == 200 ? this.handler : // Success, parse (if any)
                                          status == 201 ? this.handler : // Created, parse (if any)
                                          status == 204 ? this.handler : // No content, parse (SuccessContentHandler, for example)
                                          status == 300 ? new SiblingsContentHandler<T>() : // Siblings, parse
                                          status == 304 ? new NullContentHandler<T>() : // Not modified, discard
                                          status == 404 ? new NullContentHandler<T>() : // Not found, discard
                                              new ErrorContentHandler<T>(); // All other errors, fail

        /* Create our output, and a completion handler for the response */
        output = handler.getOutputStream(partial);
        future.addFuture(client.getExecutorService().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        future.set(handler.call());
                    } catch (Throwable throwable) {
                        future.fail(null);
                    }
                }
            }));

        /* No matter what, always continue */
        return STATE.CONTINUE;

    }

    @Override
    public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart)
    throws Exception {
        log.trace("Received body part for request %s", bodyPart.getUrl());
        bodyPart.writeTo(output);
        return STATE.CONTINUE;
    }

    @Override
    public Response<T> onCompleted()
    throws Exception {
        log.trace("Completed processing of %s", request.getUrl());

        try {
            output.close();
        } catch (Throwable throwable) {
            future.fail(throwable);
        }

        return future.get();
    }

    @Override
    public void onThrowable(Throwable throwable) {
        log.error(throwable, "Request: %s %s -> %d", request.getMethod(), request.getRawUrl(), status);

        /* First, try to kill the Response<?> future */
        try {
            future.fail(throwable);
        } catch (Throwable fail) {
            /* Could throw CancellationException/IllegalStateException */
            log.error(fail, "Exception failing response future for request %s", request.getUrl());
        }

        /* Then close the output */
        try {
            if (output != null) output.close();
        } catch (Throwable fail) {
            /* Could throw IOException */
            log.error(fail, "Exception closing handler stream for request %s", request.getUrl());
        }

    }
}
